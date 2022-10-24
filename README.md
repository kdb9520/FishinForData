# FishinForData

# CSCI 320 Group Project




# Setup Instructions
## On IntelliJ
To add the jars to the build path for an IntelliJ environment:
1. Go to File > Project Structure > Modules.
2. Click on "Dependencies" tab.
3. Click the plus symbol (+) and select "Jars or Directories".
4. Both jars should be in the 'lib' folder in the FishinForData project.
5. Select 'jsch-0.1.55.jar'
6. Repeat steps 3-5 and this time select the 'postgresql-42.5.0.jar' jar.
7. If errors appear inside PreparedStatements, add the data source to the
project using the same steps as for DataGrip, as IntelliJ is trying to scan
the SQL within the PreparedStatements without being able to see the schema.

# Command to Remove AdminAccount.java from being updated
git update-index --assume-unchanged src/AdminAccount.java