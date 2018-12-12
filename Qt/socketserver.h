#ifndef SOCKETSERVER_H
#define SOCKETSERVER_H

#include <QWidget>
#include <QtNetwork/QTcpServer>
#include <QtNetwork/QTcpSocket>
#include <QTcpServer>
#include <QTcpSocket>
#include <QList>

#include <iostream>
#include <vector>

#include "des.h"

namespace Ui {
class SocketServer;
}

class SocketServer : public QWidget
{
    Q_OBJECT

public:
    explicit SocketServer(QWidget *parent = 0);
    ~SocketServer();

private slots:
    void startListen();
    void startConnection();
    void closeConnection();
    void sendData();
    void receiveData();

private:
    Ui::SocketServer *ui;
    QTcpServer *tcpServer;
    QTcpSocket * tcpSocket;
    QList<QTcpSocket *> socketList;

    QString password;
    DES encrypt,decrypt;

    void generatePassword();
    void sendToOthers(const QString &,const int &);
};

#endif // SOCKETSERVER_H
