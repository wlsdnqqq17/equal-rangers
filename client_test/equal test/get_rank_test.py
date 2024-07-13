# import requests
# import json
# def get_rank_list():
#     rank_url = 'http://52.78.68.85:8000/api/ranks/'  # 서버의 URL에 맞게 변경해야 합니다.
    
#     with open('tokens.json', 'r') as f:
#         tokens = json.load(f)
#     print(tokens)

#     headers = {
#     'Authorization': f'Bearer {tokens["access"]}'
#     }

#     try:
#         # response = requests.get(url)
#         # print("RESPONSE:",response)
#         # response.raise_for_status()  # HTTP 요청이 성공적으로 처리되지 않으면 예외 발생
#         # print("CHECK")
#         # rank_list = response.json()  # JSON 데이터를 Python 객체로 변환
#         # print(f'HTTP RETURN: {rank_list}')
#         response = requests.get(rank_url, headers=headers)
#         print('Rank Response:', response.json())
#         return rank_list
        
#     except requests.exceptions.HTTPError as http_err:
#         print(f'HTTP error occurred: {http_err}')
#     except Exception as err:
#         print(f'Other error occurred: {err}')

# # 클라이언트 코드 실행 예시
# if __name__ == '__main__':
#     rank_list = get_rank_list()
#     if rank_list:
#         print("Rank 리스트:")
#         for rank in rank_list:
#             print(f"User ID: {rank['user_id']}, Score: {rank['score']}")


import requests
import json
import time

# 서버 URL 설정
base_url = 'http://52.78.68.85:8000'
token_url = f'{base_url}/api/token/'
refresh_url = f'{base_url}/api/token/refresh/'

# 로그인 정보
login_data = {
    'username': 'new_user1234',
    'password': 'password1234'
}

# 로그인 및 토큰 요청
response = requests.post(token_url, data=login_data)
tokens = response.json()

# 토큰 저장
with open('tokens.json', 'w') as f:
    json.dump(tokens, f)

if response.status_code == 200:
    print("Login successful!")
else:
    print(f"Login failed with status code {response.status_code}")

# 액세스 토큰 만료까지 대기 (5초 대기)
time.sleep(5)

# API 요청 함수
def get_rank_list():
    rank_url = f'{base_url}/api/ranks/'

    with open('tokens.json', 'r') as f:
        tokens = json.load(f)

    access_token = tokens['access']
    headers = {
        'Authorization': f'Bearer {access_token}'
    }

    response = requests.get(rank_url, headers=headers)

    if response.status_code == 401:
        # 액세스 토큰이 만료된 경우
        print('Access token expired. Refreshing token...')
        refresh_response = requests.post(refresh_url, data={'refresh': tokens['refresh']})
        if refresh_response.status_code == 200:
            new_tokens = refresh_response.json()
            with open('tokens.json', 'w') as f:
                json.dump(new_tokens, f)
            headers['Authorization'] = f'Bearer {new_tokens["access"]}'
            response = requests.get(rank_url, headers=headers)
        else:
            print('Failed to refresh access token.')

    if response.status_code == 200:
        rank_list = response.json()
        return rank_list
    else:
        print(f"Failed to get rank list. Status code: {response.status_code}")
        return None

# 클라이언트 코드 실행 예시
if __name__ == '__main__':
    rank_list = get_rank_list()
    if rank_list:
        print("Rank 리스트:")
        for rank in rank_list:
            print(f"User ID: {rank['user_id']}, Score: {rank['score']}")
