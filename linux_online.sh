#!/bin/sh

#Linux usually needs root permissions to sniff on network adapters
if [ "$(id -u)" != "0" ]; then
    echo "Sorry, you are not root."
    exit 1
fi

#Check if a git update is available
if [ -d ".git" ]; then
    echo "git repo found, checking for updates..."
    UPSTREAM=${1:-'@{u}'}
    LOCAL=$(git rev-parse @)
    REMOTE=$(git rev-parse "$UPSTREAM")
    BASE=$(git merge-base @ "$UPSTREAM")

    if [ $LOCAL = $REMOTE ]; then
        echo "Up-to-date"
    elif [ $LOCAL = $BASE ]; then
        echo "Update found, updating"
        git pull
        mvn verify install
    fi
fi


#Set java path's for the root user
export JAVA_HOME="/opt/jdk"
export PATH="$PATH:$JAVA_HOME/bin"

#Check if java is found
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ];  then
    echo "Found java executable in JAVA_HOME"
else
    echo "Could not find java :("
    exit 1
fi

#List openvpn bound ip's on this server:
server_bound_ips=$(ifconfig -a | grep -A 1 as0t | grep -o -E 'addr:[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')

echo "The script found the following openvpn bound IP adresses:"
for ip in $server_bound_ips
do
    ip=$(echo $ip | grep -o -E '[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}')
    echo "\t$ip"
done

#This is the openvpn ip of your server
echo "This PC's IP (172.27.XXX.1 [openvpn]):"
read server_ip

#This is the openvpn ip of the client you are playing on (displayed when you open click on "Show Status"
echo "Remote IP (172.27.XXX.X [openvpn]):"
read remote_ip

#execute the radar with the user given server and remote ip
java -jar target/Gaydar-6.9-jar-with-dependencies.jar $server_ip PortFilter $remote_ip

