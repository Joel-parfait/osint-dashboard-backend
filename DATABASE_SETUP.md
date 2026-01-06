\# Database Setup Instructions



This document explains how to set up the MongoDB database for the OSINT Dashboard.



\## Prerequisites



\- MongoDB installed (version 5.0 or higher)

\- MongoDB must be running on localhost:27017



\## Step 1: Start MongoDB



\### Windows:

MongoDB should start automatically as a service. To verify:

1\. Press `Windows Key + R`

2\. Type `services.msc`

3\. Find "MongoDB Server" - it should say "Running"



\### Linux/Mac:

```bash

sudo systemctl start mongodb

```



\## Step 2: Create MongoDB User



Open Command Prompt (Windows) or Terminal (Linux/Mac) and run:



```bash

mongosh

```



Then run these commands inside mongosh:



```javascript

use admin



db.createUser({

&nbsp; user: "kenji",

&nbsp; pwd: "Rolosha@123",

&nbsp; roles: \[ { role: "readWrite", db: "leaks\_db" } ]

})



exit

```



\## Step 3: Import the Database



\### Windows:



```bash

cd "C:\\Program Files\\MongoDB\\Server\\7.0\\bin"



mongorestore --uri="mongodb://kenji:Rolosha@123@127.0.0.1:27017/leaks\_db?authSource=admin" --nsInclude="leaks\_db.\*" "C:\\path\\to\\osint-dashboard\\database-backup\\leaks\_db"

```



\*\*Note:\*\* Replace `C:\\path\\to\\osint-dashboard` with the actual path where you cloned the project.



\### Linux/Mac:



```bash

mongorestore --uri="mongodb://kenji:Rolosha@123@127.0.0.1:27017/leaks\_db?authSource=admin" --nsInclude="leaks\_db.\*" /path/to/osint-dashboard/database-backup/leaks\_db

```



\*\*Note:\*\* Replace `/path/to/osint-dashboard` with the actual path where you cloned the project.



\## Step 4: Verify the Import



```bash

mongosh "mongodb://kenji:Rolosha@123@127.0.0.1:27017/leaks\_db?authSource=admin"

```



Inside mongosh, run:



```javascript

show collections

db.leakeddata.countDocuments()

exit

```



You should see:

\- A list of collections (leakeddata, users, sources, etc.)

\- A number showing how many documents are in the database



\## Troubleshooting



\### Error: "Authentication failed"

Make sure you created the user in Step 2 correctly.



\### Error: "Connection refused"

Make sure MongoDB is running (see Step 1).



\### Error: "Database not found"

The import might have failed. Check the path to the database-backup folder is correct.



\## Default User Credentials



After database import, you can login to the application with:



\- \*\*Username:\*\* `admin`

\- \*\*Password:\*\* `admin123`



⚠️ \*\*IMPORTANT:\*\* Change the default password after first login!



\## Questions?



If you encounter any issues during setup, please contact me.

