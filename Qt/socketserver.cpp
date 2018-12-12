#include "socketserver.h"
#include "ui_socketserver.h"

SocketServer::SocketServer(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::SocketServer)
{
    ui->setupUi(this);
    ui->lineEdit_text->setText("");
    ui->textBrowser->setText("");

    tcpServer = nullptr;
    tcpSocket = nullptr;

    setWindowTitle("Server");

    tcpServer = new QTcpServer(this);

    socketList.clear();
    //start listen
    QObject::connect(ui->pushButton_listen,&QPushButton::clicked,this,&SocketServer::startListen);

    //start connection
    QObject::connect(tcpServer,&QTcpServer::newConnection,this,&SocketServer::startConnection);

    //close connection
    QObject::connect(ui->pushButton_stop,&QPushButton::clicked,this,&SocketServer::closeConnection);

    //send data
    QObject::connect(ui->pushButton_sendText,&QPushButton::clicked,this,&SocketServer::sendData);

}

SocketServer::~SocketServer()
{
    closeConnection();
    delete ui;
    delete tcpSocket;
    delete tcpServer;
    socketList.clear();
    ui = nullptr;
    tcpSocket = nullptr;
    tcpServer = nullptr;
}

void SocketServer::startListen()
{
    //get and listen the port user input
    qint16 port = ui->lineEdit_port->text().toInt();
    tcpServer->listen(QHostAddress::LocalHost,port);
}

void SocketServer::startConnection()
{
    //connect a new client
    tcpSocket = tcpServer->nextPendingConnection();
    QString ip = tcpSocket->peerAddress().toString();
    qint16 port = tcpSocket->peerPort();
    QString temp = QString("[%1:%2]:Connect Successfully").arg(ip).arg(port);
    ui->label_networkStatus->setText(temp);

    tcpSocket->write(password.toUtf8().data());

    //receive data
    QObject::connect(tcpSocket,&QTcpSocket::readyRead,this,&SocketServer::receiveData);

    socketList.append(tcpSocket);
    //std::cout<< socketList.size() << std::endl;
}

void SocketServer::closeConnection()
{
    int i;
    //get socket connection and clost it one by one
    for(i=0;i<socketList.size();++i)
    {
        tcpSocket = socketList.at(i);
        if(tcpSocket != nullptr)
        {
            tcpSocket->disconnectFromHost();
            tcpSocket->close();
            tcpSocket = nullptr;
        }
    }

    //update connection info
    QString temp("Connection Status:No Clients");
    ui->label_networkStatus->setText(temp);
}

void SocketServer::sendData()
{
    //send data to clients
    //get and solve data
    QString content = ui->lineEdit_text->text();
    std::string t = '|' + content.toStdString();
    content = QString::fromStdString(t);
    encrypt.setValue(1,content,password);
    content = encrypt.encrypt();

    //send data one by one
    int i;
    for(i=0;i<socketList.size();++i)
    {
        tcpSocket = socketList.at(i);
        if(tcpSocket != nullptr)
        {
            tcpSocket->write(content.toUtf8().data());
        }
    }

    //std::cout<< "Server Send : "<< content.toStdString() << std::endl;
    //display data
    content = QString("You :") + content + QString("\n");
    ui->textBrowser->append(content);
    ui->lineEdit_text->setText("");
}

void SocketServer::receiveData()
{
    //receive data from clients
    int i;
    QString content;
    for(i=0;i<socketList.size();++i)
    {
        //std::cout<< "check : " << i << std::endl;
        tcpSocket = socketList.at(i);
        if(tcpSocket != nullptr)
        {
            QString temp(tcpSocket->readAll());
            if(!(temp.isEmpty()))
            {
                content = temp;
                break;
            }
        }
    }
    sendToOthers(content,i);
    //std::cout<<" send over " << content.size() << "  " << content.toStdString() << std::endl;
    //ui->textBrowser->append(content);

    decrypt.setValue(0,content,password);
    content = decrypt.decrypt();
    content = QString("Received Client Message:") + content + QString("\n");

    //std::cout<< "Server Receive : " << content.toStdString() << std::endl;
    ui->textBrowser->append(content);
}

void SocketServer::sendToOthers(const QString &content,const int &p)
{
    //send data to other cliends, no index p socket
    int i;
    for(i=0;i<socketList.size();++i)
    {
        if(i == p) continue;
        tcpSocket = socketList.at(i);
        if(tcpSocket != nullptr)
        {
            tcpSocket->write(content.toUtf8().data());
        }
    }
}

void SocketServer::generatePassword()
{
    //generate random password
    std::string passwordStd = "";
    srand(time(NULL));
    for(int i=0;i<8;++i)
    {
        char c = rand()%10 + '0';
        passwordStd += c;
    }
    password = QString::fromStdString(passwordStd);
}
