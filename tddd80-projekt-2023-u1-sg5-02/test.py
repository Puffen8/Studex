import requests
import json

# # Get the data from the API
url = "http://127.0.0.1:5000/"

# Create a new account
data = {"username": "Elle69", 
        "password": "test",
        "email": "Elliotana@testmail.com",
        "phone_number": "531503203",
        "first_name": "Elliotana",
        "last_name": "Söderströmana"}

r = requests.post(url + "signup", json=data, verify=False)
print(r.text)

# login to that account
data = {"username": "Elle69", "password": "test"}
r2 = requests.post(url + "login", json=data, verify=False)
print(r2.text)

# Get the token
token = r2.json()["token"]
# print(token, "---------------------------------------------")

# Create a new post
# Send header with token
headers = {"Content-Type": "application/json", "Authorization": "Bearer " + token}
data = {"title": "Diskret Matematik", "description": "Bok skriven av bästa Armen", "price": 69, "program_id": 2, "course_id": 3}
r3 = requests.post(url + "listing/add", json=data, verify=False, headers=headers)
print(r3.text)

# Get the listing id
listing_id = r3.json()["listing_id"]

# Create another account
data = {"username": "Kerre", "password": "test", "email": "lol@loller.se", "phone_number": "072-1525631", "first_name": "Kevin", "last_name": "Rintanen Österblad"}
r4 = requests.post(url + "signup", json=data, verify=False)

# Login to that account
data = {"username": "Kerre", "password": "test"}
r5 = requests.post(url + "login", json=data, verify=False)

# Get the token
token2 = r5.json()["token"]

# Send header with token
headers2 = {"Content-Type": "application/json", "Authorization": "Bearer " + token2}

# Create new chat on the post

# print(r3.json()["listing_id"] + "---------------------------------------------")
data = {"listing_id": r3.json()["listing_id"]}
r6 = requests.post(url + "listing/" + data["listing_id"] + "/new_chat", json=data, verify=False, headers=headers2)
print(r6.text)

# Get the chat id
chat_id = r6.json()["chat_id"]

# Send a message to the chat as the buyer
data = {"chat_id": chat_id, "message": "Hello Elliotana, I am interested in your book!"}
r7 = requests.post(url + "messages/" + data["chat_id"] + "/send", json=data, verify=False, headers=headers2)
print(r7.text)

# Send a message to the chat as the seller
data = {"chat_id": chat_id, "message": "Hello Kerre, I am happy to hear that! I will send you my address!"}
r8 = requests.post(url + "messages/" + data["chat_id"] + "/send", json=data, verify=False, headers=headers)
print(r8.text)

# Send a message to the chat as the buyer
data = {"chat_id": chat_id, "message": "Great! I will send you the money!"}
r9 = requests.post(url + "messages/" + data["chat_id"] + "/send", json=data, verify=False, headers=headers2)
print(r9.text)

# Send a message to the chat as the seller
data = {"chat_id": chat_id, "message": "Thank you! I will send the book as soon as I get the money!"}
r10 = requests.post(url + "messages/" + data["chat_id"] + "/send", json=data, verify=False, headers=headers)
print(r10.text)

# Send a message to the chat as the buyer
data = {"chat_id": chat_id, "message": "Great! I will send the money as soon as possible!"}
r11 = requests.post(url + "messages/" + data["chat_id"] + "/send", json=data, verify=False, headers=headers2)
print(r11.text)

# Get the messages from the chat
r12 = requests.get(url + "messages/" + chat_id, verify=False, headers=headers)
print(r12.text)

print("-------------------------------------------------------------")
# Get all chats
r121 = requests.get(url + "chats/all", verify=False, headers=headers)
print(r121.text)
print("-------------------------------------------------------------")
# Get all chats
r122 = requests.get(url + "chats/all", verify=False, headers=headers2)
print(r122.text)
print("-------------------------------------------------------------")

# Get listings from a specific user
r131 = requests.get(url + "listings/user/" + "Elle69" , verify=False, headers=headers)
print(r131.text)

# Get listings from a specific user
r141 = requests.get(url + "listings/user/" + "Kerre" , verify=False, headers=headers2)
print(r141.text)


# Logout from the buyer account
r13 = requests.post(url + "logout", verify=False, headers=headers2)
print(r13.text)

# Edit the post
data = {"title": "Odiskret Matematik", "description": "Bok skriven av bästa Benet", "price": 420, "program_id": 3, "course_id": 1, "listing_id": r3.json()["listing_id"]}
r14 = requests.put(url + "listing/edit/" + listing_id, json=data, verify=False, headers=headers)
print(r14.text)

# Get listing page
r15 = requests.get(url + "listing/" + listing_id, verify=False, headers=headers2)
print(r15.text)


# Login to the buyer account
data = {"username": "Kerre", "password": "test"}
r16 = requests.post(url + "login", json=data, verify=False)
print(r16.text)

# Get the token
token3 = r16.json()["token"]

# Send header with token
headers3 = {"Content-Type": "application/json", "Authorization": "Bearer " + token3}

# Add Elles post to favorites
data = {"listing_id": listing_id}
r17 = requests.post(url + "listing/" + listing_id + "/favourite", json=data, verify=False, headers=headers3)
print(r17.text)

# Try adding the same post to favorites again
data = {"listing_id": listing_id}
r18 = requests.post(url + "listing/" + listing_id + "/favourite", json=data, verify=False, headers=headers3)
print(r18.text)

# Try unfavouriting the post
data = {"listing_id": listing_id}
r19 = requests.delete(url + "listing/" + listing_id + "/favourite", json=data, verify=False, headers=headers3)
print(r19.text)

# View all listings
r20 = requests.get(url + "listings", verify=False, headers=headers3)
print(r20.text)

# Get favorites
r21 = requests.get(url + "listings/favorites", verify=False, headers=headers3)
print(r21.text)

# # Remove the post
# r22 = requests.delete(url + "listing/delete/" + listing_id, verify=False, headers=headers)
# print(r22.text)

# Create a new user
data = {"username": "Denhärskabort", "password": "test", "email": "aaah@hotmail.com"}
r23 = requests.post(url + "signup", json=data, verify=False)
print(r23.text)

# Login to the new user
data = {"username": "Denhärskabort", "password": "test"}
r24 = requests.post(url + "login", json=data, verify=False)
print(r24.text)

# Get the token
token4 = r24.json()["token"]

# Send header with token
header4 = {"Content-Type": "application/json", "Authorization": "Bearer " + token4}

# Edit profile
data = {"username": "Denhärskabort", "first_name": "Kalle", "last_name": "Anka", "phone_number": "070420420"}
r25 = requests.put(url + "profile", json=data, verify=False, headers=header4)
print(r25.text)

# Get profile
r26 = requests.get(url + "profile", verify=False, headers=header4)
print(r26.text)

# Delete profile
r27 = requests.delete(url + "profile/delete", verify=False, headers=header4)
print(r27.text)

# Get deleted profile
r28 = requests.get(url + "profile", verify=False, headers=header4)
print(r28.text)

# Get listing page
r15 = requests.get(url + "listing/" + listing_id, verify=False, headers=headers3)
print(r15.text)

# Get unviewed posts for Kerre
r29 = requests.get(url + "listings/unviewed", verify=False, headers=headers3)
print(r29.text)

# Create listing
data = {"title": "Biologi suger", "description": "Bok om biologi", "price": 499.99, "program_id": 3, "course_id": 1}
r30 = requests.post(url + "listing/add", json=data, verify=False, headers=headers3)
print(r30.text)

print("-------------------------------------------------------------")
# Search for listings
# query = "/listings/search?query=biologi"
r31 = requests.get(url + "listings/search?query=bästa", json=data, verify=False, headers=headers3)
print(r31.text)
print("-------------------------------------------------------------")





