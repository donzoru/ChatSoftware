#include "socketclient.h"
#include "socketserver.h"

#include <QApplication>

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);

    SocketServer s1;
    s1.show();

    SocketClient c1;
    c1.show();

    SocketClient c2;
    c2.show();
    return a.exec();
}
