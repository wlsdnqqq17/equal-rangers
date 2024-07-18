# 이퀄레인저
- 네 자리 숫자를 가지고 등식을 만드는 게임
- 이쿼리들이 자동차의 번호판에 붙어 등식을 만들면 자동차의 오래된 부품을 하나 빼내고 새로운 부품으로 교체합니다.
- 제한시간 동안 많은 자동차의 부품을 얻는 것이 이 게임의 목표입니다.
- 총 11가지의 연산자
    - 더하기, 빼기, 곱하기, 나누기, 제곱근, 세제곱근, 제곱, 세제곱, 이어붙이기, 음수 만들기, 같다..
    

# 디자인

## 이쿼리

![스크린샷 2024-07-17 오후 6.38.22.png](https://github.com/user-attachments/assets/e4afbd14-da04-47f4-bc4f-f00956eeaf26)

![스크린샷 2024-07-17 오후 7.10.58.png](https://github.com/user-attachments/assets/6c57ba52-68c1-4e35-9997-ed8af021d51c)

- 기본 형태 : 홈 화면, 상점, 게임화면 버튼
- 확장 형태: 본 게임 화면 (등식 만들 때)

## 스플래시 화면
![f](https://github.com/user-attachments/assets/a2cb1dee-5140-4383-9b08-3d35daddae88)
- 이쿼리들이 자동차를 배경으로 날아가는 이미지를 표현

## 홈 화면

![f](https://github.com/user-attachments/assets/d50c8e85-a83b-434b-9870-ad164e783413)

- 애니메이션

# 로그인

# 홈 화면



- 홈 화면에서는 유저의 닉네임, 최고 점수, 부품(게임 내 화폐)등을 확인하실 수 있습니다
- 랭킹 버튼을 누르면 서버 내의 다양한 유저들의 점수와 비교해 볼 수 있습니다
- 상점을 누르면 부품을 사용해서 이쿼리들을 소환하실 수 있습니다
- 소환하신 이쿼리들은 홈 화면에서 뛰어놀고 있습니다
- 이쿼리들은 성격마다 뛰는 스타일이 모두 다릅니다. 모든 이쿼리들을 수집해봅시다!
  
# 게임

## 문제 선택 화면

![image](https://github.com/user-attachments/assets/ab30a73b-2ac5-4fd3-ac63-ba2ad328204a)

- 게임 시작 버튼을 누르면 문제 선택 화면으로 넘어갑니다.
- 문제는 어려운 문제와 쉬운 문제가 섞여서 나오기 때문에 세 가지 문제 중 하나를 선택할 수 있게 했습니다.
- 총 주어진 시간은 60초입니다.
- 문제를 고르면 시간은 멈추지만 5초 차감됩니다.
- 문제를 푸는 데 성공하면 5초를 돌려 받고 문제를 풀지 못해 뒤로가기 버튼을 누른다면 5초를 돌려받지 못합니다.

## 문제 풀이 화면
![problem1](https://github.com/user-attachments/assets/37eaf25f-9d48-41da-b797-1e06ba10b831)
![problem2](https://github.com/user-attachments/assets/ebfa5d4d-de2d-4811-ad9b-628945947869)
![problem3](https://github.com/user-attachments/assets/9d0ef61d-00b8-4685-94ce-b39e2a876f9f)

- 문제를 클릭하면 시간이 멈추고 문제를 풀 수 있는 공간에 들어옵니다.
- 화면 상단에서 풀 문제를 볼 수 있습니다.
- 화면 중앙에는 드래그 앤 드롭 할 수 있는 숫자들이 있습니다.
- 화면 하단에는 이쿼리들이 있습니다.
- 이쿼리를 터치하면 이쿼리들이 확장되어 빈 공간에 나타납니다.
- 숫자 혹은 완성된 식을 확장된 이쿼리들에게 드래그 앤 드롭하면 식을 만들 수 있습니다.
- 확장된 이쿼리들이 필요 없어졌으면 삭제할 수 있습니다.
- 숫자가 들어있는 이쿼리는 삭제할 수 없습니다.
- 숫자가 들어있는 이쿼리는 먼저 분해해서 숫자를 뺀 다음에 삭제할 수 있습니다.
- 언제나 마지막은 두 식을 등호 연산자에 집어넣어 참을 확인해야 합니다.
- 참이 확인되면 부품을 하나 얻고 사용한 연산자의 종류에 따라 점수를 얻을 수 있습니다.

## 결과 화면

![스크린샷 2024-07-17 오후 8 25 55](https://github.com/user-attachments/assets/76911b0f-d53d-4726-8eef-dc99c637dc96)


- 주어진 시간이 모두 종료되면 결과 화면으로 넘어옵니다.
- 결과 화면에서는 내 점수 기록과 얻은 부품 수를 확인할 수 있습니다.

# 구현 기술

## 문제 데이터셋 제작

![스크린샷 2024-07-17 오후 7.01.30.png](https://github.com/user-attachments/assets/4170a7d0-06dc-4240-8801-da4bfdd0a87c)

- 등식을 만들 수 있는 수는 몇 개인가?
- 분할 정복 알고리즘
- 2+2 or 3+1
- 길이 n(1,2,3)인 문자열을 계산해서 만들 수 있는 값을 재귀적으로 계산
- 네자리 문자열 중 풀 수 있는 등식의 개수는 **7847개**

## 서버 통신 기술

- 클라이언트는 서버에게서 AccessToken과 RefreshToken을 발급받습니다
- AccessToken은 짧은 시간동안만 지속되고, RefreshToken은 비교적 오래 지속됩니다
- AccessToken으로 서버에 API 실행을 요청하고, AccessToken이 만료되면 RefreshToken으로 재발급을 요청합니다
- RefreshToken까지 만료되면 다시 로그인 해야합니다!


# **기술 스택**

Frontend: AndroidStudio/Kotlin
Backend: Django Restframework/AWS
Algorithm: Python

# **애플리케이션**
https://drive.google.com/file/d/1-BtKX_LGgfzCage7xRpcnDlRrwhNJH47/view?usp=sharing
