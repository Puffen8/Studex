import re
from datetime import timedelta

from flask import request, jsonify
from flask_jwt_extended import (
    jwt_required, create_access_token, get_jwt, get_jwt_identity
)

from data_handler import (
    db, User, bcrypt, app, Message, TokenBlocklist, Listing, jwt, Chat,
    Program, Course
)

"""
This file contains the main code for the backend of the web application.
It contains the code for the user authentication, user profile management,
and the listings management.
"""

# _________________________________________
# ---------- JWT token management ----------

@jwt.token_in_blocklist_loader
def check_if_token_revoked(jwt_header, jwt_payload: dict) -> bool:
    """
    Function that checks if the token is revoked.
    A token is revoked if it is in the blocklist.
    """
    jti = jwt_payload["jti"]
    token = db.session.query(TokenBlocklist.id).filter_by(jti=jti).scalar()
    return token is not None

# ______________________________
# ---------- Homepage ---------- 

@app.route("/", methods=["GET"])
@jwt_required(optional=True)
def home_page(): 
    """
    Function that handles what happens when the user
    visits the home page.
    """
    current_user = get_jwt_identity()
    if current_user:
        return jsonify({"message": "Welcome to Studex!", "status": 
                        "You are logged in as {}".format(current_user)}), 200
    return jsonify({"message": "Welcome to Studex!", "status": 
                    "You are not logged in. Please make an account."}), 200

# _________________________________________
# ---------- User authentication ---------- 

@app.route("/login", methods=["POST"])
def login_page(): 
    """
    Function that handles the login process for users.
    """
    data = request.get_json()
    username = data["username"].lower()
    user = User.query.filter_by(username=username).first()
    if user and bcrypt.check_password_hash(user.password, data["password"]):
        access_token = create_access_token(identity=user.serialize(), expires_delta=timedelta(hours=1))
        user.login()
        return jsonify({"message": "Successfully logged in", "token": access_token, "id": user.id}), 200
    return jsonify({"message": "Invalid username or password."}), 401


@app.route("/signup", methods=["POST"])
def signup_page(): 
    """
    Function that handles the signup process for users.
    """
    data = request.get_json()

    # Check if username is valid
    username = data["username"].lower()
    if not username:
        return jsonify({"message": "Username is required."}), 400
    if User.query.filter_by(username=username).first():
        return jsonify({"message": "Username already exists."}), 400
    if not username.isalnum():
        return jsonify({"message": "Username must only contain letters and numbers."}), 400
    if len(username) > 20 or len(username) < 3:
        return jsonify({"message": "Username must be between 3 and 20 characters long."}), 400
    
    # Check if password is valid        
    password = data["password"]
    if len(password) < 8:
        return jsonify({"message": "Password must be at least 8 characters long."}), 400
    if not any(char.isdigit() for char in password):
        return jsonify({"message": "Password must contain at least one number."}), 400
    if not any(char.isupper() for char in password):
        return jsonify({"message": "Password must contain at least one uppercase letter."}), 400
    if not any(char.islower() for char in password):
        return jsonify({"message": "Password must contain at least one lowercase letter."}), 400
    
    # Check if first name is valid
    first_name=data.get("first_name")
    if first_name and not first_name.isalpha():
        return jsonify({"message": "First name must only contain letters."}), 400
    if first_name and len(first_name) > 20:
        return jsonify({"message": "First name must be at most 20 characters long."}), 400
    
    # Check if last name is valid
    last_name=data.get("last_name")
    if last_name and not last_name.isalpha():
        return jsonify({"message": "Last name must only contain letters."}), 400
    if last_name and len(last_name) > 30:
        return jsonify({"message": "Last name must be at most 30 characters long."}), 400
    
    # Check if phone number is valid
    phone_number=data.get("phone_number")
    if User.query.filter_by(phone_number=data.get("phone_number")).first() and data.get("phone_number"):
        return jsonify({"message": "Phone number is already used."}), 400
    if phone_number and not phone_number.replace(" ", "").replace("-", "").replace("(", "").replace(")", "").replace("+", "").isdigit():
        return jsonify({"message": "Phone number must only contain numbers."}), 400
    if phone_number and len(phone_number) > 20:
        return jsonify({"message": "Phone number must be at most 20 characters long."}), 400
    
    # Check if email is valid
    email=data.get("email")
    if not email:
        return jsonify({"message": "Email is required."}), 400
    email_pattern = r"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$"
    if not re.match(email_pattern, email):
        return jsonify({"message": "Email is invalid."}), 400
    if User.query.filter(User.email == email).first() is not None: 
        return jsonify({"message": "Email is already used."}), 400

    new_user = User(username=username, 
                    password=data["password"], 
                    email=data["email"], 
                    first_name=data.get("first_name"), 
                    last_name=data.get("last_name"), 
                    phone_number=data.get("phone_number"))

    db.session.add(new_user)

    db.session.commit()
    return jsonify({"message": "Successfully signed up"}), 200


@app.route("/logout", methods=["POST"])
@jwt_required()
def logout_page(): 
    """
    Function that handles the logout process for users.
    """
    identity = get_jwt_identity()
    db.session.add(TokenBlocklist(jti=get_jwt()["jti"]))
    user = User.query.filter_by(id=identity['id']).first()
    user.logout()
    db.session.commit()
    return jsonify({"message": "Successfully logged out"}), 200

# __________________________________
# ---------- User profile ----------
 
@app.route("/profile", methods=["GET"])
@jwt_required()
def profile_page(): 
    """
    The function that handles the process of showing the user profile.
    """
    identity = get_jwt_identity()
    user = User.query.filter_by(id=identity['id']).first()
    return jsonify(user.serialize()), 200


@app.route("/profile", methods=["PUT"])
@jwt_required()
def edit_profile_page(): 
    """
    Function that handles the process of editing a user profile.
    That also handles if some fields are not filled in.
    """
    identity = get_jwt_identity()
    user = User.query.filter_by(id=identity['id']).first()
    data = request.get_json()

    if user.phone_number != data.get("phone_number"):
        if User.query.filter_by(phone_number=data.get("phone_number")).first() and data.get("phone_number"):
            return jsonify({"message": "Phone number is already used."}), 400
        if data.get("phone_number") and not data.get("phone_number").replace(" ", "").replace("-", "").replace("(", "").replace(")", "").replace("+", "").isdigit():
            return jsonify({"message": "Phone number must only contain numbers."}), 400
        if data.get("phone_number") and len(data.get("phone_number")) > 20:
            return jsonify({"message": "Phone number must be at most 20 characters long."}), 400
        user.phone_number = data.get("phone_number", user.phone_number)

    if user.first_name != data.get("first_name"):
        if data.get("first_name") and not data.get("first_name").isalpha():
            return jsonify({"message": "First name must only contain letters."}), 400
        if data.get("first_name") and len(data.get("first_name")) > 20:
            return jsonify({"message": "First name must be at most 20 characters long."}), 400
        user.first_name = data.get("first_name", user.first_name)

    if user.last_name != data.get("last_name"):
        if data.get("last_name") and not data.get("last_name").isalpha():
            return jsonify({"message": "Last name must only contain letters."}), 400
        if data.get("last_name") and len(data.get("last_name")) > 30:
            return jsonify({"message": "Last name must be at most 30 characters long."}), 400
        user.last_name = data.get("last_name", user.last_name)

    db.session.commit()
    return jsonify({"message": "Profile updated successfully"}), 200


@app.route("/profile/delete", methods=["DELETE"])
@jwt_required()
def delete_profile_page():
    """
    Function that handles the process of deleting a user profile.
    The deletion of the user profile also deletes all the listings
    which is handled by the cascade delete in the database.
    """
    identity = get_jwt_identity()
    user = User.query.filter_by(id=identity['id']).first()
    db.session.add(TokenBlocklist(jti=get_jwt()["jti"]))
    db.session.delete(user)
    db.session.commit()
    return jsonify({"message": "Account deleted successfully"}), 200


# Get user profile by id
@app.route("/profile/<user_id>", methods=["GET"])
def get_user_profile(user_id):
    """
    Function that fetches a user profile.
    """
    user = User.query.filter_by(id=user_id).first()
    return jsonify(user.serialize()), 200

# _________________________________________
# ---------- Listings management ---------- 

@app.route("/listing/add", methods=["POST"])
@jwt_required()
def add_listing_page():
    """
    Function that handles the process of adding a new book listing.
    """
    user = get_jwt_identity()
    data = request.get_json()

    new_listing = Listing(
        title=data.get("title"), 
        price=data.get("price"), 
        owner_id=user["id"],
        location=data.get("location"),
        description=data.get("description"),
        image=data.get("image"))

    if new_listing.title is None or len(new_listing.title) == 0:
        return jsonify({"message": "Title is missing"}), 400
    if new_listing.title and len(new_listing.title) > 30:
        return jsonify({"message": "Title must be at most 30 characters long."}), 400
    if new_listing.description and len(new_listing.description) > 240:
        return jsonify({"message": "Description must be at most 240 characters long."}), 400
    if new_listing.location and len(new_listing.location) > 150:
        return jsonify({"message": "Location must be at most 150 characters long."}), 400
    if new_listing.price is None:
        return jsonify({"message": "Price is missing"}), 400
    if not new_listing:
        return jsonify({"message": "Listing could not be created"}), 400

    db.session.add(new_listing)
    db.session.commit()
    return jsonify({"message": "Listing has been posted", "listing_id": new_listing.id}), 200


@app.route("/listing/edit/<ListingID>", methods=["PUT"])
@jwt_required()
def edit_listing_page(ListingID):
    """
    Function that handles the process of editing a book listing.
    """
    user = get_jwt_identity()
    listing = Listing.query.filter_by(id=ListingID).first()

    if not listing:
        return jsonify({"message": "Listing not found"}), 400
    if listing.owner_id != user['id']:
        return jsonify({"message": "You are not the owner of this listing"}), 400
    data = request.get_json()   

    listing.title = data.get("title", listing.title)
    listing.price = str(data.get("price", listing.price))
    listing.location = data.get("location", listing.location)
    listing.description = data.get("description", listing.description)
    listing.image = data.get("image", listing.image)

    if listing.title and len(listing.title) > 30:
        return jsonify({"message": "Title must be at most 30 characters long."}), 400
    if listing.description and len(listing.description) > 240:
        return jsonify({"message": "Description must be at most 240 characters long."}), 400
    if listing.location and len(listing.location) > 150:
        return jsonify({"message": "Location must be at most 150 characters long."}), 400
    if listing.price and len(listing.price) < 0 or len(listing.price) > 6:
        return jsonify({"message": "Price must be between 0 and 999999 kr."}), 400

    db.session.commit()
    return jsonify({"message": "Listing updated successfully", "listing_id": listing.id}), 200


@app.route("/listing/delete/<ListingID>", methods=["DELETE"])
@jwt_required()
def delete_listing_page(ListingID): 
    """
    Function that handles the process of deleting a book listing.
    """
    listing = Listing.query.filter_by(id=ListingID).first()
    identity = get_jwt_identity()
    if listing:
        if listing.owner_id != identity['id']:
            return jsonify({"message": "You are not the owner of this listing"}), 400
        db.session.delete(listing)
        db.session.commit()
        return jsonify({"message": "Listing deleted successfully"}), 200
    return jsonify({"message": "Listing was not found."}), 400


@app.route("/listing/<ListingID>", methods=["GET"])
@jwt_required(optional=True)
def listing_page(ListingID): 
    """
    Function that handles the process of getting a book listing.
    """
    identity = get_jwt_identity()
    listing = Listing.query.filter_by(id=ListingID).first()
    if listing:
        # if identity:
        #     user = User.query.filter_by(id=identity["id"]).first()
        #     if listing not in user.viewed_listings:
        #         user.viewed_listings.append(listing)
        #         db.session.commit()
        #         print("Listing added to viewed listings")
        return jsonify(listing.serialize()), 200
    return jsonify({"message": "Listing was not found."}), 400

@app.route("/listing/<ListingID>/view", methods=["POST"])
@jwt_required()
def view_listing_page(ListingID):
    """
    Function that handles the process of adding a book listing to viewed listings.
    """
    listing = Listing.query.filter_by(id=ListingID).first()
    identity = get_jwt_identity()
    user = User.query.filter_by(id=identity["id"]).first()
    if listing and identity:
        if listing in user.viewed_listings:
            return jsonify({"message": "Listing is already in viewed listings."}), 400
        user.viewed_listings.append(listing)
        db.session.commit()
        return jsonify({"message": "Listing added to viewed listings."}), 200
    return jsonify({"message": "Listing was not found."}), 400

@app.route("/listing/<ListingID>/favorite", methods=["POST"])
@jwt_required()
def favorite_listing_page(ListingID): 
    """
    Function that handles the process of adding a book listing to favorites.
    """
    listing = Listing.query.filter_by(id=ListingID).first()
    identity = get_jwt_identity()
    user = User.query.filter_by(id=identity["id"]).first()
    if listing and identity:
        if listing in user.favorites:
            return jsonify({"message": "Listing is already in favorites."}), 400
        user.favorites.append(listing)
        db.session.commit()
        return jsonify({"message": "Listing added to favorites."}), 200
    return jsonify({"message": "Listing was not found."}), 400


@app.route("/listing/<ListingID>/favorite", methods=["DELETE"])
@jwt_required()
def unfavorite_listing_page(ListingID): 
    """
    Function that handles the process of removing a book listing from favorites.
    """
    listing = Listing.query.filter_by(id=ListingID).first()
    identity = get_jwt_identity()
    user = User.query.filter_by(id=identity["id"]).first()
    if listing and user:
        if listing not in user.favorites:
            return jsonify({"message": "Listing is not in favorites."}), 400
        user.favorites.remove(listing)
        db.session.commit()
        return jsonify({"message": "Listing removed from favorites."}), 200
    return jsonify({"message": "Listing was not found."}), 400


@app.route("/listings", methods=["GET"])
@jwt_required(optional=True)
def listings_page(): 
    """
    Function that handles the process of displaying all book listings.
    """
    listings = Listing.query.all()
    if listings:
        return jsonify([listing.serialize() for listing in listings]), 200
    return jsonify({"message": "No listings found."}), 400


@app.route("/listings/favorites", methods=["GET"])
@jwt_required()
def favorites_page(): 
    """
    Function that handles the process of displaying all favorite book listings.
    """
    identity = get_jwt_identity()
    user = User.query.filter_by(id=identity["id"]).first()
    if user:
        return jsonify([listing.serialize() for listing in user.favorites]), 200
    return jsonify({"message": "No favorites found."}), 400


@app.route("/listings/user/<Username>", methods=["GET"])
@jwt_required(optional=True)
def user_listings_page(Username): 
    """
    Function that handles the process of displaying all book listings by a specific user.
    """
    user = User.query.filter_by(username=Username).first()
    if user and user.listings:
        return jsonify([listing.serialize() for listing in user.listings]), 200
    return jsonify({"message": "No listings found."}), 400


@app.route("/listings/unviewed", methods=["GET"])
@jwt_required()
def unviewed_listings_page():
    """
    Function that handles the process of displaying all book listings that
    have not been viewed by the user.
    """
    identity = get_jwt_identity()
    user = User.query.filter_by(id=identity["id"]).first()
    unviewed_listings = Listing.query.filter(~Listing.viewed_by.contains(user)).all()

    if unviewed_listings:
        return jsonify([listing.serialize() for listing in unviewed_listings]), 200

    return jsonify({"message": "No unviewed listings found."}), 200

@app.route("/listings/viewed", methods=["GET"])
@jwt_required()
def viewed_listings_page():
    """
    Function that handles the process of displaying all book listings that
    have been viewed by the user.
    """
    identity = get_jwt_identity()
    user = User.query.filter_by(id=identity["id"]).first()
    viewed_listings = user.viewed_listings

    if viewed_listings:
        return jsonify([listing.serialize() for listing in viewed_listings]), 200

    return jsonify({"message": "No viewed listings found."}), 200

@app.route("/listings/search", methods=["GET"])
@jwt_required(optional=True)
def search_listings_page():
    """
    Function that handles the process of searching for book listings.
    """
    query = request.args.get("query")
    listings = Listing.query.filter(db.or_(Listing.title.ilike(f"%{query}%"), 
                                           Listing.description.ilike(f"%{query}%"),
                                           Listing.location.ilike(f"%{query}%")
                                           )).all()
    if listings:
        return jsonify([listing.serialize() for listing in listings]), 200
    return jsonify({"message": "No listings found."}), 400


@app.route("/listings/program/<ProgramID>", methods=["GET"])
@jwt_required(optional=True)
def program_listings_page(ProgramID):
    """
    Function that handles the process of displaying all book listings of a specific program.
    """
    program = Program.query.filter_by(id=ProgramID).first()
    if program:
        return jsonify([listing.serialize() for listing in program.listings]), 200
    return jsonify({"message": "No listings found."}), 400


@app.route("/listings/course/<CourseID>", methods=["GET"])
@jwt_required(optional=True)
def course_listings_page(CourseID):
    """
    Function that handles the process of displaying all book listings of a specific course.
    """
    course = Course.query.filter_by(id=CourseID).first()
    if course:
        return jsonify([listing.serialize() for listing in course.listings]), 200
    return jsonify({"message": "No listings found."}), 400

# ______________________________
# ---------- Chatting ---------- 

@app.route("/listing/<ListingID>/new_chat", methods=["POST"])
@jwt_required(optional=True)
def new_chat_page(ListingID): 
    """
    Function that handles the process of creating a new chat.
    """
    user = get_jwt_identity()
    if user:
        listing = Listing.query.filter_by(id=ListingID).first()
        if not listing:
            return jsonify({"message": "Listing was not found."}), 400
        if listing.owner_id == user["id"]:
            return jsonify({"message": "You cannot chat with yourself."}), 400
        new_chat = Chat(buyer_id=user["id"], seller_id=listing.owner_id, listing_id=listing.id)
        db.session.add(new_chat)
        db.session.commit()
        return jsonify(new_chat.serialize()), 200
    return jsonify({"message": "You must be logged in to chat."}), 400


@app.route("/messages/<ChatID>", methods=["GET"])
@jwt_required()
def chat_page(ChatID):
    """
    Function that handles the process of displaying a chat.
    """
    user = get_jwt_identity()
    chat = Chat.query.filter_by(id=ChatID).first()
    if not chat:
        return jsonify({"message": "Chat was not found."}), 400
    if chat.buyer_id != user["id"] and chat.seller_id != user["id"]:
        return jsonify({"message": "You are not part of this chat."}), 400
    
    messages = Message.query.filter_by(chat_id=ChatID).all()

    if messages:
        return jsonify([message.serialize() for message in messages]), 200
    
    return jsonify({"message": "No messages found."}), 400


@app.route("/messages/<ChatID>/send", methods=["POST"])
@jwt_required()
def send_message_page(ChatID):
    """
    Function that handles the process of sending a message.
    """
    user = get_jwt_identity()
    chat = Chat.query.filter_by(id=ChatID).first()
    if not chat:
        return jsonify({"message": "Chat was not found."}), 400
    if chat.buyer_id != user["id"] and chat.seller_id != user["id"]:
        return jsonify({"message": "You are not part of this chat."}), 400
    data = request.get_json()
    new_message = Message(
        message=data["message"],
        chat_id=ChatID,
        author_id=user["id"])
    if not new_message:
        return jsonify({"message": "Message could not be created"}), 400
    db.session.add(new_message)
    db.session.commit()
    return jsonify({"message": "Message sent successfully", "message_id": new_message.id}), 200


@app.route("/chats/all", methods=["GET"])
@jwt_required()
def all_chats_page():
    """
    Function that handles the process of displaying all chats for a user.
    """
    user = get_jwt_identity()
    chats = Chat.query.filter(db.or_(Chat.buyer_id==user["id"], Chat.seller_id==user["id"])).all()
    if not chats:
        return jsonify({"message": "No chats found."}), 400
    return jsonify([chat.serialize() for chat in chats]), 200

# ----------------------------------------------------------------------------

if __name__ == "__main__":
    app.debug = True
    app.run(port='5000')


# TODO: Add image upload to profile (profile picture)
# TODO: Add image upload to chat
# TODO: Add a user review/rating system?
# TODO: Improve search algorithm for searching listings
# TODO: Add course and program categories to listings and search
# TODO: Fix user online status


