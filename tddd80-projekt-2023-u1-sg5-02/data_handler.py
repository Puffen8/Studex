import os
import re
import sys
from datetime import datetime, timedelta, timezone
from flask import Flask
from flask_bcrypt import Bcrypt
from flask_jwt_extended import JWTManager
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy.orm import validates
import secrets

"""
This file contains the database models for the web application.
It contains the models for the user, listings, and messages.
"""

app = Flask(__name__)
bcrypt = Bcrypt(app)

cur_dir = os.path.dirname(os.path.abspath(__file__))

if "pytest" in sys.modules:
    db_uri = "sqlite:///" + cur_dir + "/test_database.db"
elif 'WEBSITE_HOSTNAME' in os.environ:  # running on Azure: use postgresql
    database = os.environ['DBNAME']  # postgres
    host_root = '.postgres.database.azure.com'
    host = os.environ['DBHOST'] + host_root  # app-name + root
    user = os.environ['DBUSER']
    password = os.environ['DBPASS']
    db_uri = f'postgresql+psycopg2://{user}:{password}@{host}/{database}'
    debug_flag = False
else: # when running locally: use sqlite
    db_path = os.path.join(os.path.dirname(__file__), 'database.db')
    db_uri = "sqlite:///" + cur_dir + "/database.db"
    debug_flag = True

ACCESS_EXPIRES = timedelta(hours=1)
app.config["JWT_ACCESS_TOKEN_EXPIRES"] = ACCESS_EXPIRES

app.config['SQLALCHEMY_DATABASE_URI'] = db_uri    
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = True
app.config['JWT_SECRET_KEY'] = "Elliotana"

jwt = JWTManager(app)
db = SQLAlchemy(app)

# -------------------------------------DATABASE START-------------------------------------

course_listings = db.Table("course_listings", # This is the table that connects the listing and the course
    db.Column("listing_id", db.String(12), db.ForeignKey("listing.id"), primary_key=True),
    db.Column("course_id", db.Integer, db.ForeignKey("course.id"), primary_key=True))

program_listings = db.Table("program_listings", # This is the table that connects the listing and the program
    db.Column("listing_id", db.String(12), db.ForeignKey("listing.id"), primary_key=True),
    db.Column("program_id", db.Integer, db.ForeignKey("program.id"), primary_key=True))

favorite_listings = db.Table("favorite_listings", # This is the table that connects the user and the listing
    db.Column("user_id", db.String(12), db.ForeignKey("user.id"), primary_key=True),
    db.Column("listing_id", db.String(12), db.ForeignKey("listing.id"), primary_key=True))

viewed_listings = db.Table("viewed_listings", # This is the table that connects the user and the listing
    db.Column("user_id", db.String(12), db.ForeignKey("user.id"), primary_key=True),
    db.Column("listing_id", db.String(12), db.ForeignKey("listing.id"), primary_key=True))

class User(db.Model):
    """
    This class represents the user model.
    """
    id = db.Column(db.String(12), primary_key=True)
    created_at = db.Column(db.DateTime, nullable=True)
    username = db.Column(db.String(30), unique=True, nullable=False)
    password = db.Column(db.String(128), nullable=False)
    email = db.Column(db.String(120), unique=True)
    phone_number = db.Column(db.String(30), nullable=True)
    first_name = db.Column(db.String(50), nullable=True)
    last_name = db.Column(db.String(50), nullable=True)
    last_seen = db.Column(db.DateTime, nullable=True)
    online = db.Column(db.Boolean, nullable=False)

    listings = db.relationship("Listing", backref=db.backref("owner", lazy=True))
    
    chats_as_buyer = db.relationship("Chat", backref=db.backref("buyer", lazy=True), foreign_keys="Chat.buyer_id")
    chats_as_seller = db.relationship("Chat", backref=db.backref("seller", lazy=True), foreign_keys="Chat.seller_id")

    favorites = db.relationship("Listing", secondary=favorite_listings, backref=db.backref("favorited_by", lazy=True))
    viewed_listings = db.relationship("Listing", secondary=viewed_listings, backref=db.backref("viewed_by", lazy=True))

    def __init__(self, username, password, email, phone_number=None, first_name=None, last_name=None, online=False):
        
        self.id = secrets.token_hex(12)
        self.username = username
        self.password = password
        self.email = email
        self.phone_number = phone_number
        self.first_name = first_name
        self.last_name = last_name
        self.online = online
        self.created_at = datetime.now(timezone.utc)

    @validates("username")
    def validate_username(self, key, username):
        """
        This method validates the username.
        """
        if len(username) < 4:
            raise ValueError("Username must be between 4 and 30 characters long")
        pattern = r'^[0-9a-zA-ZåäöÅÄÖ]+$'
        if not re.match(pattern, username):
            raise ValueError("Username can only contain: A-ö, a-ö or 0-9")
        return username
    
    @validates("password")
    def validate_password(self, key, password):
        """
        This method validates the password.
        """
        if len(password) < 8:
            raise ValueError("Password must be at least 8 characters long")
        dig, upp, low = False, False, False
        for char in password:
            if char.isdigit():
                dig = True
            if char.isupper():
                upp = True
            if char.islower():
                low = True
        if dig and upp and low:
            return bcrypt.generate_password_hash(password).decode('utf-8')
        raise ValueError("Password must contain: digit, lower and uppercase")

    @validates("email")
    def validate_email(self, key, email):
        """
        This method validates the email.
        """
        if not email:
            raise ValueError("Email address is required")
        email_pattern = r"^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}$"
        if not re.match(email_pattern, email):
            raise ValueError("Invalid email address")
        if User.query.filter(User.email == email).first() is not None:
            raise ValueError("Email address is already in use")
        return email
    
    @validates("phone_number")
    def validate_phone_number(self, key, pho_num):
        """
        This method validates the phone_number.
        """
        if pho_num:
            if len(pho_num) > 0:
                if User.query.filter_by(phone_number=pho_num).first():
                    raise ValueError("Phone number is already in use")
        return pho_num

    def __repr__(self):
        return f"{self.username}"
    
    def serialize(self):
        return {
            "id": self.id,
            "username": self.username,
            "email": self.email,
            "first_name": self.first_name,
            "last_name": self.last_name,
            "phone_number": self.phone_number,
            "created_at": self.created_at
        }
    
    def login(self):
        """
        This method updates the user's last seen time to the current time when the user logs in.
        """
        self.last_seen = datetime.now(timezone.utc)
        self.online = True
        db.session.commit()

    def logout(self):
        """
        This method updates the user's last seen time to the current time when the user logs out.
        """
        self.last_seen = datetime.now(timezone.utc)
        self.online = False
        db.session.commit()

    def is_online(self):
        """
        This method checks whether the user is currently online.
        """
        if self.last_seen is None:
            return False
        now = datetime.now(timezone.utc)
        # If the user was last seen lessthan 10 minutes ago, consider them online
        return (now - self.last_seen) < timedelta(minutes=10) 
    
class Listing(db.Model):
    """
    This class represents the listing model.
    """
    id = db.Column(db.String(12), primary_key=True)
    title = db.Column(db.String(60), nullable=False)
    price = db.Column(db.Float, nullable=False)
    location = db.Column(db.String(30), nullable=True)
    description = db.Column(db.String(240), nullable=True)
    created_at = db.Column(db.DateTime, nullable=False)
    image = db.Column(db.String(1000000), nullable=True)

    owner_id = db.Column(db.String(12), db.ForeignKey("user.id"), nullable=False)
    
    course = db.relationship("Course", secondary=course_listings)
    program = db.relationship("Program", secondary=program_listings)

    chats = db.relationship("Chat", backref=db.backref("listing", lazy=True)) 

    def __init__(self, title, location, price=0, description = None, owner_id = None, image = None):
        self.id = str(secrets.token_hex(12))
        self.title = title
        self.price = price
        self.location = location
        self.description = description
        self.owner_id = owner_id
        self.created_at = datetime.now(timezone.utc)
        self.image = image

    def __repr__(self):
        return f"{self.id}"

    def serialize(self):
        return {
            "id": self.id,
            "title": self.title,
            "price": self.price,
            "location": self.location,
            "description": self.description,
            "created_at": self.created_at,
            "owner_id": self.owner_id,
            "image": self.image
        }
    
    
class Course(db.Model):
    """
    This class represents the course search category model.
    """
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(80), unique=False)
    program_id = db.Column(db.Integer, db.ForeignKey("program.id"), nullable = False)

    def __init__(self, name):
        self.name = name

    def __repr__(self):
        return f"{self.name}"
    

class Program(db.Model):
    """
    This class represents the program search category model.
    """
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(80), unique=True)

    courses = db.relationship("Course", backref=db.backref("program", lazy=True))

    def __init__(self, name):
        self.name = name

    def __repr__(self):
        return f"{self.name}"
    

class Chat(db.Model):
    """
    This class represents the chat model.
    """
    id = db.Column(db.String(12), primary_key=True)
    listing_id = db.Column(db.String(12), db.ForeignKey("listing.id"), nullable = True)
    buyer_id = db.Column(db.String(12), db.ForeignKey("user.id"), nullable = False)
    seller_id = db.Column(db.String(12), db.ForeignKey("user.id"), nullable = False)

    messages = db.relationship("Message", backref=db.backref("chat", lazy=True))

    def __init__(self, listing_id, buyer_id, seller_id, messages = []):
        self.id = secrets.token_hex(12)
        self.listing_id = listing_id
        self.buyer_id = buyer_id
        self.seller_id = seller_id
        self.messages = messages

    def __repr__(self):
        return f"{self.id}"
    
    def serialize(self):
        return {
            "id": self.id,
            "listing_id": self.listing_id,
            "buyer_id": self.buyer_id,
            "seller_id": self.seller_id,
            "messages": [message.serialize() for message in self.messages]
        }
    

class Message(db.Model):
    """
    This class represents the message model.
    """
    id = db.Column(db.String(12), primary_key=True)
    timestamp = db.Column(db.DateTime, nullable=False)
    message = db.Column(db.String(140), nullable=False)
    author_id = db.Column(db.String(12), db.ForeignKey('user.id'), nullable=False)
    chat_id = db.Column(db.String(12), db.ForeignKey("chat.id"), nullable=False)

    def __init__(self, message, author_id, chat_id):
        self.id = secrets.token_hex(12)
        self.timestamp = datetime.now()
        self.message = message
        self.author_id = author_id
        self.chat_id = chat_id

    def __repr__(self):
        return f"{self.id}"
    
    def serialize(self):
        return {
            "id": self.id,
            "timestamp": self.timestamp,
            "message": self.message,
            "author_id": self.author_id,
            "chat_id": self.chat_id
        }


class TokenBlocklist(db.Model):
    """
    This class represents the token blocklist model.
    """
    id = db.Column(db.Integer, primary_key=True)
    jti = db.Column(db.String(36), nullable=False, index=True)
    revoked_at = db.Column(db.DateTime, nullable=False)

    def __init__(self, jti):
        self.jti = jti
        self.revoked_at = datetime.now()

    def __repr__(self):
        return f"{self.id}"
    
    def is_revoked(self):
        return self.revoked_at is not None
    
    def revoke(self):
        self.revoked_at = datetime.now()
        db.session.commit()

    def unrevoke(self):
        self.revoked_at = None
        db.session.commit()

# --------------------------------------DATABASE END--------------------------------------

def init_db():
    """o
    This method initializes the database.
    """
    db.drop_all()
    db.create_all()
    db.session.commit()