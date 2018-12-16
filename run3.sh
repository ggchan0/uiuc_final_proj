set -e
if [ $# -lt 3 ]
  then
    echo "Usage: run.sh <Users.xml> <Posts.xml> <Comments.xml> <Optional # of users>"
    exit
fi

javac *.java

if [ $# -eq 3 ]
  then
   java -DentityExpansionLimit=0 -DtotalEntitySizeLimit=0 -Djdk.xml.totalEntitySizeLimit=0 parseUsers $1 $2 $3
elif [ $# -eq 4 ]
  then
    java -DentityExpansionLimit=0 -DtotalEntitySizeLimit=0 -Djdk.xml.totalEntitySizeLimit=0 parseUsers $1 $2 $3 $4
else
  echo "Incorrect number of arguments. Usage: run.sh <Users.xml> <Posts.xml> <Comments.xml> <Optional # of users>"
fi
