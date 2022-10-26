import requests

def notion_integration(title, description, date, time):
    url = "https://api.notion.com/v1/pages"

    database = "9cb67b9d407648f5bedcf02607619bda"

    secret_token = "secret_Pa1sHlxLhH6gLoCwLA2pWt8rOZFjXwqPHklurR6etqY"

    headers = {
        "Notion-Version": '2022-06-28',
        "content-type": "application/json",
        "authorization": f"Bearer {secret_token}"
    }

    data_input = {
        "parent": { "database_id": f"{database}" },
        "properties": {
            "Name": {
                "title": [
                    {
                        "text": {
                            "content": f"{title}"
                        }
                    }
                ]
            },
            "Place": {
                "rich_text": [
                    {
                        "text": {
                            "content": f"{description}"
                        }
                    }
                ]
            },
            "Date": {
                "date":
                {
                    "start": f"{date}"
                }
            },
            "Time": {
                "rich_text": [
                    {
                        "text": {
                            "content": f"{time}"
                        }
                    }
                ]
            }
        }
    }

    response = requests.post(url, headers=headers, json=data_input)
    print(response.json())