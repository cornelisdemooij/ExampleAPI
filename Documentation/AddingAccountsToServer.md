# Adding an account for a human user

```
sudo adduser mike                            # Create user 'mike'
sudo usermod -aG sudo mike                   # Give sudo privileges to 'mike'
sudo mkdir /home/mike/.ssh                   # Create the folder where ssh gets config for 'mike'
sudo nano /home/mike/.ssh/authorized_keys    # Install (copy-paste) public key of Mike
sudo chown -R mike:mike /home/mike/.ssh      # Change the owner and group of Mike's .ssh folder and its contents to mike:mike
```

When adding a user, an initial password will have to be chosen. When the user logs in for the first time, they can change their own password with the following command:
passwd
