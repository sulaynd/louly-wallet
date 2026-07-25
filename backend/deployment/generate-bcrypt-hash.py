#!/usr/bin/env python3
"""
Generates a bcrypt hash for use in harden-for-production.sql — run locally, never send a real
password anywhere else (no online generators, no pasting into chat).

Setup (once):
    pip install bcrypt --break-system-packages

Usage:
    python3 generate-bcrypt-hash.py
    (it will prompt for the password without echoing it to the screen)
"""
import getpass
import bcrypt

password = getpass.getpass("New password (won't be shown): ")
confirm = getpass.getpass("Confirm: ")

if password != confirm:
    print("Passwords didn't match — try again.")
else:
    hashed = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt(rounds=10)).decode("utf-8")
    print("\nBcrypt hash (paste this into harden-for-production.sql):")
    print(hashed)
