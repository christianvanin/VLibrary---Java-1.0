import sys
import requests
import random
import string
import re
import json
from pathlib import Path

def main():
    sys.stdout.reconfigure(encoding='utf-8')
    sys.stderr.reconfigure(encoding='utf-8')
    
    try:
        if len(sys.argv) < 2:
            print("[ERROR] Nessun file di input fornito", file=sys.stderr)
            sys.exit(1)
        
        input_file = Path(sys.argv[1])
        if not input_file.exists():
            print(f"[ERROR] File non trovato: {input_file}", file=sys.stderr)
            sys.exit(1)

        input_text = input_file.read_text(encoding='utf-8')
        if not input_text.strip():
            print("[ERROR] Input vuoto ricevuto", file=sys.stderr)
            sys.exit(1)
        
        session = requests.Session()

        resp = session.get("https://monica.im/")
        html = resp.text

        device_id_match = re.search(r'"device_id":"([a-z0-9-]+)"', html)
        client_id_match = re.search(r'"x-client-id":"([a-z0-9-]+)"', html)

        device_id = ''.join(random.choices(string.ascii_letters + string.digits, k=16))
        client_id = client_id_match.group(1) if client_id_match else "default-client"

        payload = {
            "task_uid": "rewriter:576e981c-d817-4a31-9998-1061926ada65",
            "data": {
                "content": input_text,
                "mode": "standard",
                "use_model": "gpt-4o-mini",
                "intensity": "medium",
                "language": "auto",
                "device_id": device_id
            },
            "language": "auto",
            "locale": "it",
            "task_type": "seotool:ai_rewrite"
        }

        headers = {
            "content-type": "application/json",
            "x-client-id": client_id,
            "x-client-locale": "it",
            "x-client-type": "web",
            "x-client-version": "5.4.3",
            "x-product-name": "Monica"
        }

        url = "https://api.monica.im/api/seotool/ai_rewrite"
        response = session.post(url, headers=headers, data=json.dumps(payload), timeout=30)
        response.encoding = 'utf-8'

        if response.status_code != 200:
            print(f"[ERROR] Richiesta fallita: {response.status_code}", file=sys.stderr)
            print(f"[ERROR] Response: {response.text}", file=sys.stderr)
            sys.exit(1)

        try:
            json_response = response.json()
            if "text" in json_response:
                result_text = json_response["text"]
            elif "data" in json_response and "text" in json_response["data"]:
                result_text = json_response["data"]["text"]
            else:
                result_text = str(json_response)
            print(result_text)
            
        except json.JSONDecodeError:
            full_text = ""
            for line in response.text.split('\n'):
                if line.startswith("data: "):
                    try:
                        chunk = json.loads(line[6:])
                        full_text += chunk.get("text", "")
                    except json.JSONDecodeError:
                        pass
            
            if full_text:
                print(full_text)
            else:
                print(response.text)

    except Exception as e:
        print(f"[ERROR] Errore: {str(e)}", file=sys.stderr)
        import traceback
        traceback.print_exc(file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    main()
