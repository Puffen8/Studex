import os
import tempfile

import pytest

import app
import data_handler

base_url = "http://127.0.0.1:5000"

@pytest.fixture
def client():
    db_fd, app.app.config['DATABASE_FILE_PATH'] = tempfile.mkstemp()
    app.app.config['SQLALCHEMY_DATABASE_URI'] = "sqlite:///" + app.app.config['SQLALCHEMY_DATABASE_URI']
    app.app.config['TESTING'] = True

    client = app.app.test_client()


    with app.app.app_context():
        data_handler.init_db()

    yield client

    os.close(db_fd)
    os.unlink(app.app.config['DATABASE_FILE_PATH'])

def test_home_page(client):
    # Accessing the home page when not logged in
    r = client.get('/', content_type='application/json')
    assert r.status_code == 200
    assert r.get_json()['message'] == 'Welcome to Studex!'

    # Accessing the home page when logged in
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'
    phone_number = '1234567890'
    first_name = 'testfirst'
    last_name = 'testlast'
    payload = {'username': username, 'password': password, 'email': email,
    'phone_number': phone_number, 'first_name': first_name, 'last_name': last_name}
    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}
    r = client.post('/login', json=payload, content_type='application/json')
    token = r.get_json()['token']

    r = client.get('/', content_type='application/json', headers={"Authorization": "Bearer " + token})
    assert r.status_code == 200
    assert r.get_json()['message'] == 'Welcome to Studex!'


def test_signup_page(client):
    # Signing up a user with only mandatory data
    payload = {'username': 'testuser1', 'password': '123abcABC', 'email': 'test1@gmail.com'}
    r = client.post('/signup', json=payload, content_type='application/json')
    assert r.status_code == 200
 
    # Signing up a user with optional data
    payload = {'username': 'testuser2', 'password': '123abcABC', 'email': 'test2@gmail.com',
        'phone_number': '1234567890', 'first_name': 'testfirst', 'last_name': 'testlast'}
    r = client.post('/signup', json=payload, content_type='application/json')
    assert r.status_code == 200

    # Signing up a user with an existing username
    payload = {'username': 'testuser1', 'password': '123abcABC', 'email': 'test3@gmail.com'}
    r = client.post('/signup', json=payload, content_type='application/json')
    assert r.status_code == 400
    assert r.get_json() == {'message': 'Username already exists.'}

    # Signing up a user with an existing email
    payload = {'username': 'testuser3', 'password': '123abcABC', 'email': 'test1@gmail.com'}
    r = client.post('/signup', json=payload, content_type='application/json')
    assert r.status_code == 400


def test_login_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    payload = {'username': username, 'password': password, 'email': 'test1@gmail.com'}
    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}
    r = client.post('/login', json=payload, content_type='application/json')
    assert r.status_code == 200
    assert r.get_json()['message'] == 'Successfully logged in'

    # Logging in the user with the wrong password
    payload = {'username': username, 'password': 'WrongPassword123'}
    r = client.post('/login', json=payload, content_type='application/json')
    assert r.status_code == 401
    assert r.get_json() == {'message': 'Invalid username or password.'}

    # Logging in the user with the wrong username
    payload = {'username': 'wrongusername', 'password': password}
    r = client.post('/login', json=payload, content_type='application/json')
    assert r.status_code == 401
    assert r.get_json() == {'message': 'Invalid username or password.'}


def test_logout_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    payload = {'username': username, 'password': password, 'email': 'test1@gmail.com'}
    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}
    r = client.post('/login', json=payload, content_type='application/json')
    token = r.get_json()['token']

    # Logging out the user
    r = client.post('/logout', headers={"Authorization": "Bearer " + token})
    assert r.status_code == 200

    # Logging out the same user again
    r = client.post('/logout', headers={"Authorization": "Bearer " + token})
    assert r.status_code == 401


def test_profile_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'
    phone_number = '1234567890'
    first_name = 'testfirst'
    last_name = 'testlast'
    payload = {'username': username, 'password': password, 'email': email,
    'phone_number': phone_number, 'first_name': first_name, 'last_name': last_name}
    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}
    r = client.post('/login', json=payload, content_type='application/json')
    token = r.get_json()['token']

    # Accessing the profile page of the user
    r = client.get('/profile', headers={"Authorization": "Bearer " + token})
    assert r.status_code == 200
    assert r.get_json()['username'] == username
    assert r.get_json()['email'] == email
    assert r.get_json()['phone_number'] == phone_number
    assert r.get_json()['first_name'] == first_name
    assert r.get_json()['last_name'] == last_name


def test_edit_profile_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'
    phone_number = '1234567890'
    first_name = 'testfirst'
    last_name = 'testlast'
    payload = {'username': username, 'password': password, 'email': email,
    'phone_number': phone_number, 'first_name': first_name, 'last_name': last_name}
    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}
    r = client.post('/login', json=payload, content_type='application/json')
    token = r.get_json()['token']

    # Editing all of the users information
    new_first_name = 'newtestfirst'
    new_last_name = 'newtestlast'
    new_phone_number = '01234567890'
    new_password = 'new123abcABC'
    payload = {'password': new_password, 'phone_number': new_phone_number,
    'first_name': new_first_name, 'last_name': new_last_name}
    
    r = client.put('/profile', json=payload, headers={"Authorization": "Bearer " + token})
    assert r.status_code == 200
    assert r.get_json() == {"message": "Profile updated successfully"}

    # Editing only one of the users information
    new_password = 'new123abcABC'
    payload = {'password': new_password, 'phone_number': new_phone_number,
    'first_name': new_first_name, 'last_name': new_last_name}
    
    r = client.put('/profile', json=payload, headers={"Authorization": "Bearer " + token})
    assert r.status_code == 200
    assert r.get_json() == {"message": "Profile updated successfully"}


def test_delete_profile_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'

    payload = {'username': username, 'password': password, 'email': email}
    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}
    r = client.post('/login', json=payload, content_type='application/json')
    token = r.get_json()['token']

    # Deleting the user
    r = client.delete('profile/delete', headers={"Authorization": "Bearer " + token})
    assert r.status_code == 200

def test_add_listing_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'

    payload = {'username': username, 'password': password, 'email': email}
    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}
    r = client.post('/login', json=payload, content_type='application/json')
    token = r.get_json()['token']

    # Adding a listing
    listing_title = 'testlisting'
    price = '123'
    payload = {'price': price, 'title': listing_title}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token})
    assert r.status_code == 200
    assert r.get_json()['message'] == 'Listing has been posted'
    assert len(r.get_json()['listing_id']) > 0

    # Adding a listing with all of the optional data
    listing_title = 'testlisting'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token})

    # Adding a listing without a title
    price = '123'
    payload = {'price': price}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'Title is missing'}


def test_edit_listing_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'
    payload = {'username': username, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token1 = r.get_json()['token']

    # Adding a listing with all of the optional data
    listing_title = 'testlisting'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})
    listing_id = r.get_json()['listing_id']

    # Editing all of the listings information
    new_listing_title = 'newtestlisting'
    new_price = '1123'
    new_location = 'newtestlocation'
    new_description = 'newtestdescription'
    payload = {'price': new_price, 'title': new_listing_title, 'location': new_location, 'description': new_description}

    r = client.put(f'/listing/edit/{listing_id}', json=payload, headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200

    # Editing only one of the listings information
    new_listing_title = 'newtestlisting2'
    payload = {'title': new_listing_title}

    r = client.put(f'/listing/edit/{listing_id}', json=payload, headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200

     # Editing a listing with the wrong listing id
    new_listing_title = 'newtestlisting2'
    payload = {'title': new_listing_title}

    r = client.put('/listing/edit/wronglistingid', json=payload, headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'Listing not found'}

    # Signing up another user
    password = '123abcABC'
    username = 'testuser2'
    email = 'test2@gmail.com'
    payload = {'username': username, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the new user
    payload = {'username': username, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token2 = r.get_json()['token']

    # Editing the listing with another user
    new_listing_title = 'newtestlisting2'
    payload = {'title': new_listing_title}

    r = client.put(f'/listing/edit/{listing_id}', json=payload, headers={"Authorization": "Bearer " + token2})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'You are not the owner of this listing'}
    

def test_delete_listing_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'
    payload = {'username': username, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token1 = r.get_json()['token']

    # Adding a listing with all of the optional data
    listing_title = 'testlisting'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})
    listing_id = r.get_json()['listing_id']

    # Deleting the listing
    r = client.delete(f'/listing/delete/{listing_id}', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200
    assert r.get_json()['message'] == 'Listing deleted successfully'

    # Deleting the listing with the wrong listing id
    r = client.delete('/listing/delete/wronglistingid', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'Listing was not found.'}

    # Signing up another user
    password = '123abcABC'
    username = 'testuser2'
    email = 'test2@gmail.com'
    payload = {'username': username, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the new user
    payload = {'username': username, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token2 = r.get_json()['token']

    # Deleting the listing with another user
    r = client.delete(f'/listing/delete/{listing_id}', headers={"Authorization": "Bearer " + token2})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'Listing was not found.'}


def test_listings_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'
    payload = {'username': username, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token1 = r.get_json()['token']

    # Getting the listings when there are no listings
    r = client.get('/listings', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'No listings found.'}

    # Adding a listing with all of the optional data
    listing_title1 = 'testlisting1'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title1, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})

    # Adding another listing with all of the optional data
    listing_title2 = 'testlisting2'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title2, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})

    # Getting the listings
    r = client.get('/listings', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200
    assert r.get_json()[0]['title'] == listing_title1
    assert r.get_json()[1]['title'] == listing_title2
    assert len(r.get_json()) == 2


def test_listing_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'
    payload = {'username': username, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token1 = r.get_json()['token']

    # Adding a listing with all of the optional data
    listing_title = 'testlisting'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})
    listing_id = r.get_json()['listing_id']

    # Getting the listing
    r = client.get(f'/listing/{listing_id}', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200
    assert r.get_json()['title'] == listing_title

    # Getting the listing with a non existing listing id
    r = client.get('/listing/wrongid', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'Listing was not found.'}


def test_search_listing_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'
    payload = {'username': username, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token1 = r.get_json()['token']

    # Adding a listing with all of the optional data
    listing_title = 'testlisting'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})
    listing_id = r.get_json()['listing_id']

    # Adding the listing to favorites
    r = client.post(f'listing/{listing_id}/favorite', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200
    assert r.get_json() == {'message': 'Listing added to favorites.'}

    # Adding a listing that is already in favofavoriterites to favorites
    r = client.post(f'listing/{listing_id}/favorite', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'Listing is already in favorites.'}

    # Adding a listing to favorites with the wrong listing id
    r = client.post('listing/wrongid/favorite', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'Listing was not found.'}


def test_unfavorite_listing_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'
    payload = {'username': username, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token1 = r.get_json()['token']

    # Adding a listing with all of the optional data
    listing_title = 'testlisting'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})
    listing_id = r.get_json()['listing_id']

    # Adding the listing to favorites
    r = client.post(f'listing/{listing_id}/favorite', headers={"Authorization": "Bearer " + token1})

    # Removing the listing from favorites
    r = client.delete(f'listing/{listing_id}/favorite', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200
    assert r.get_json() == {'message': 'Listing removed from favorites.'}

    # Removing a listing that is not in favorites from favorites
    r = client.delete(f'listing/{listing_id}/favorite', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'Listing is not in favorites.'}

    # Removing a listing from favorites using the wrong listing id
    r = client.delete('listing/wrongid/favorite', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'Listing was not found.'}
    

def test_favorites_page(client):
    # Signing up a user
    password = '123abcABC'
    username = 'testuser1'
    email = 'test1@gmail.com'
    payload = {'username': username, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token1 = r.get_json()['token']

    # Adding a listing with all of the optional data
    listing_title1 = 'testlisting1'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title1, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})
    listing_id = r.get_json()['listing_id']

    # Adding another listing with all of the optional data
    listing_title2 = 'testlisting2'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title2, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})

    # Getting the favorites when there are no favorites
    r = client.get('listings/favorites', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200
    assert r.get_json() == []

    # Adding the listing to favorites
    r = client.post(f'listing/{listing_id}/favorite', headers={"Authorization": "Bearer " + token1})
    
    # Getting all of the favorites
    r = client.get('listings/favorites', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200
    assert r.get_json()[0]['title'] == listing_title1
    assert len(r.get_json()) == 1


def test_user_listings_page(client):
    # Signing up a user
    password = '123abcABC'
    username1 = 'testuser1'
    email = 'test1@gmail.com'
    payload = {'username': username1, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username1, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token1 = r.get_json()['token']

    # Adding a listing with all of the optional data
    listing_title1 = 'testlisting1'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title1, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})

    # Signing up another user
    password = '123abcABC'
    username2 = 'testuser2'
    email = 'test2@gmail.com'
    payload = {'username': username2, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')

    # Logging in the new user
    payload = {'username': username2, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token2 = r.get_json()['token']

    # Adding another listing with all of the optional data
    listing_title2 = 'testlisting2'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title2, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token2})

    # Getting all of the listings posted by the first user
    r = client.get(f'listings/user/{username1}', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200
    assert r.get_json()[0]['title'] == listing_title1
    assert len(r.get_json()) == 1

    # Signing up another user
    password = '123abcABC'
    username3 = 'testuser3'
    email = 'test3@gmail.com'
    payload = {'username': username3, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')

    # Logging in the new user
    payload = {'username': username3, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token3 = r.get_json()['token']

    # Getting all of the listings posted by a user that haven't posted any listings
    r = client.get(f'listings/user/{username3}', headers={"Authorization": "Bearer " + token3})
    assert r.status_code == 400
    assert r.get_json() == {'message': 'No listings found.'}


def test_unviewed_listings_page(client):
    # Signing up a user
    password = '123abcABC'
    username1 = 'testuser1'
    email = 'test1@gmail.com'
    payload = {'username': username1, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username1, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token1 = r.get_json()['token']

    # Adding a listing with all of the optional data
    listing_title1 = 'testlisting1'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title1, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})
    listing_id1 = r.get_json()['listing_id']

    # Signing up another user
    password = '123abcABC'
    username2 = 'testuser2'
    email = 'test2@gmail.com'
    payload = {'username': username2, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')

    # Logging in the new user
    payload = {'username': username2, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token2 = r.get_json()['token']

    # Adding another listing with all of the optional data
    listing_title2 = 'testlisting2'
    price = '123'
    location = 'testlocation'
    description = 'testdescription'
    payload = {'price': price, 'title': listing_title2, 'location': location, 'description': description}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token2})
    listing_id2 = r.get_json()['listing_id']

    # Getting all of the first users unviewed listings
    r = client.get('/listings/unviewed', headers={"Authorization": "Bearer " + token1})
    assert r.status_code == 200
    assert r.get_json()[0]['title'] == listing_title1
    assert r.get_json()[1]['title'] == listing_title2
    assert len(r.get_json()) == 2


def test_search_listings_page(client):
    # Signing up a user
    password = '123abcABC'
    username1 = 'testuser1'
    email = 'test1@gmail.com'
    payload = {'username': username1, 'password': password, 'email': email}

    r = client.post('/signup', json=payload, content_type='application/json')
    
    # Logging in the user
    payload = {'username': username1, 'password': password}

    r = client.post('/login', json=payload, content_type='application/json')
    token1 = r.get_json()['token']

    # Adding a listing with all of the optional data
    listing_title1 = 'testlisting1'
    price = '123'
    location1 = 'testlocation1'
    description1 = 'testdescription1'
    payload = {'price': price, 'title': listing_title1, 'location': location1, 'description': description1}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})

    # Adding a listing with all of the optional data
    listing_title2 = 'testlisting2'
    price = '123'
    location2 = 'testlocation2'
    description2 = 'testdescription2'
    payload = {'price': price, 'title': listing_title2, 'location': location2, 'description': description2}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})

    # Adding a listing with all of the optional data
    listing_title3 = 'testlisting3'
    price = '123'
    location3 = 'testlocation3'
    description2 = 'testdescription2'
    payload = {'price': price, 'title': listing_title3, 'location': location3, 'description': description2}

    r = client.post('/listing/add', json=payload, headers={"Authorization": "Bearer " + token1})

    # Searching for listings with a certain title
    r = client.get(f'/listings/search?query={listing_title1}', content_type='application/json')
    assert r.status_code == 200
    assert r.get_json()[0]['title'] == listing_title1
    assert len(r.get_json()) == 1

    # Searching for listings with a certain description
    r = client.get(f'/listings/search?query={description2}', content_type='application/json')
    assert r.status_code == 200
    assert len(r.get_json()) == 2

    # Searching for listings with a certain description
    r = client.get('/listings/search?query=nomatchingpattern', content_type='application/json')
    assert r.status_code == 400
    assert r.get_json() == {'message': 'No listings found.'}


def test_all_chats_page(client):
    pass

def test_chat_page(client):
    pass
