# One-Time Setup

To log in, you will need a pair of keys for SSH. If you haven't created such a pair already, open a terminal (as administrator) and navigate to your .ssh folder:

```
cd ~/.ssh
```

If the .ssh folder doesn't exist yet, navigate to your user folder, create it with the following command and then navigate to it:

```
cd ~ && mkdir .ssh && cd ~/.ssh
```

Then create your pair of SSH keys with this command:

```
ssh-keygen -t rsa -b 4096 -C "mike"
```

You will see a prompt similar to the following:

```
Generating public/private rsa key pair.

Enter file in which to save the key (/c/Users/<username>/.ssh/id_rsa): 
```

Type "id_rsa_mike" and press enter. You will see this prompt: 

```
Enter passphrase (empty for no passphrase): 
```

It is recommended to use a passphrase. You will have to enter this passphrase each time you use your private key to log in.

After entering your passphrase twice, you will see output similar to the following:

```
Your identification has been saved in id_rsa_mike.

Your public key has been saved in id_rsa_mike.pub.

The key fingerprint is:

SHA256:OCRu8HJml5s6tWGZkVaDbBCF7DXMLRqZacbuA9vUHro mike

The key's randomart image is:

+---[RSA 4096]----+
|  .X=B..         |
|  oo@.= o        |
|  o+o*.o .       |
|  .+-o*o         |
|  .*Bo=*S        |
|  .*=.B+         |
|     -oo         |
|    E+.          |
|    ..           |
+----[SHA256]-----+
```

If you open your `C:/Users/<username>/.ssh` folder, you should find two files, named "id_rsa_mike" and "id_rsa_mike.pub".

The first file contains your (encrypted) private key, which you should never reveal to anyone else. 

The second file contains your public key, which you can share freely.

Send me the contents of the public key file, and I will use it to set up your account on the server.

# Logging In

After I have set up your account on the server, open a terminal window again (doesn't have to be as administrator) and type the following command:

```
ssh mike@example.com
```

The first time, you will see the following prompt:

```
The authenticity of host 'example.com (188.166.61.51)' can't be established.

ECDSA key fingerprint is SHA256:yNalbceN1nU+GZflecpdLOs3oO8S/xIgaRqIgSA5P0c.

Are you sure you want to continue connecting (yes/no/[fingerprint])?


Type "yes" and press enter. 
```

SSH should find your private key file automatically and prompt you for the passphrase.

Enter your passphrase and SSH will use your private key to prove your identity, allowing you to log in securely.
