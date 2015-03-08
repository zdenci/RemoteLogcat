#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>


const int PORT_NO=58362;

void printError( const char* pName, FILE* f = stdout )
{
	fprintf( f, "%s() failed with errno %d - %s\n", pName, errno, strerror(errno) );
}

int connect()
{
	int socketFd = socket( AF_INET, SOCK_STREAM, 0 );
	if ( socketFd < 0 )
	{
		printError( "socket" );
		return -1;
	}

	sockaddr_in serverAddr;
	serverAddr.sin_family = AF_INET;
	serverAddr.sin_port = htons( PORT_NO );
	serverAddr.sin_addr.s_addr = INADDR_ANY;
	

	if ( bind(socketFd, (sockaddr *)&serverAddr, sizeof(serverAddr)) < 0 )
	{
		close( socketFd );
		printError( "bind" );
		return -1;
	}

	if ( listen(socketFd, 1) < 0 )
	{
		close( socketFd );
		printError( "listen" );
		return -1;
	}

	return socketFd;
}
	

int main()
{
	int socketFd = connect();		
	if ( socketFd < 0 )
		return -1;

	while(1)
	{
		sockaddr_in clientAddr;
		socklen_t nSize = sizeof(clientAddr);
		int clientSock = accept( socketFd, (sockaddr *)&clientAddr, &nSize );
		if ( clientSock < 0 )
		{
			close( socketFd );
			printError( "accept" );
			return -1;
		}

		// citanie dat
		while(1)
		{
			char pBuff[256] = {0};
			ssize_t iRecv = recv( clientSock, pBuff, 256, 0 );
			if ( iRecv < 0 ) 
				printError("recv");
			if ( iRecv == 0 )
				break;
			printf("%s",  pBuff );

		}

		close( clientSock );
	}
	close( socketFd );

	return 0;
}

