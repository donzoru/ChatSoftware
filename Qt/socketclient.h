#ifndef SOCKETCLIENT_H
#define SOCKETCLIENT_H

#include <QWidget>
#include <QtNetwork/QTcpSocket>
#include <QTcpSocket>
#include <iostream>

#include "des.h"

namespace Ui {
class SocketClient;
}

class SocketClient : public QWidget
{
    Q_OBJECT

public:
    explicit SocketClient(QWidget *parent = 0);
    ~SocketClient();

private:
    Ui::SocketClient *ui;
    QTcpSocket *tcpSocket; // connect
    QString password;      // password for encrypt and edecrypt
    DES encrypt,decrypt;

private slots:
    void startConnection();
    void closeConnection();
    void sendData();
    void receiveData();
    void disconnected();
};

#endif // SOCKETCLIENT_H
