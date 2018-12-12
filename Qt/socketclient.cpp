#include "socketclient.h"
#include "ui_socketclient.h"

SocketClient::SocketClient(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::SocketClient)
{
    ui->setupUi(this);
    ui->textBrowser->setText("");
    setWindowTitle("Client");
    tcpSocket = nullptr;

    tcpSocket = new QTcpSocket(this);

    srand(time(NULL));
    tcpSocket->bind(rand()%10000+13000);

    //start connection
    QObject::connect(ui->pushButton_connect,&QPushButton::clicked,this,&SocketClient::startConnection);

    //change status
    QObject::connect(tcpSocket,&QTcpSocket::disconnected,this,&SocketClient::disconnected);

    //receive and send text
    QObject::connect(tcpSocket,&QTcpSocket::readyRead,this,&SocketClient::receiveData);
    QObject::connect(ui->pushButton_sendText,&QPushButton::clicked,this,&SocketClient::sendData);

    //close connection
    QObject::connect(ui->pushButton_disconnect,&QPushButton::clicked,this,&SocketClient::closeConnection);
}

SocketClient::~SocketClient()
{
    delete ui;
    delete tcpSocket;
    ui = nullptr;
    tcpSocket = nullptr;
}


void SocketClient::startConnection()
{
    //get ip and port
    QString ip = ui->lineEdit_ip->text();
    qint16 port = ui->lineEdit_port->text().toInt();
    tcpSocket->connectToHost(ip,port);

    //get password
    QString content(tcpSocket->readAll());
    password = content;

    //update display connection info
    QString temp = QString("[%1:%2]:Connect Successfully").arg(ip).arg(port);
    ui->label_networkStatus->setText(temp);
}

void SocketClient::closeConnection()
{
    if(tcpSocket != nullptr)
    {
        tcpSocket->disconnectFromHost();
        tcpSocket->close();
        tcpSocket = nullptr;
    }
}

void SocketClient::sendData()
{
    if(tcpSocket != nullptr)
    {
        //get and encrypt data
        QString content = ui->lineEdit_text->text();
        encrypt.setValue(1,content,password);
        content = encrypt.encrypt();
        tcpSocket->write(content.toUtf8().data());

        //std::cout<< "Client send : "<< content.toStdString() << std::endl;

        //display data
        content = QString("You :") + content + QString("\n");

        //send
        ui->textBrowser->append(content);
        ui->lineEdit_text->setText("");
    }
}

void SocketClient::receiveData()
{
    if(tcpSocket != nullptr)
    {
        //get and decrypt data
        QString content(tcpSocket->readAll());
        //std::cout<< "Client Receive : " << content.toStdString() << std::endl;
        decrypt.setValue(0,content,password);
        content = decrypt.decrypt();

        //judge source
        if(content[0].toLatin1()=='|')
        {
            std::string t = content.toStdString();
            t = t.substr(1);
            //std::cout<< t << "  " << t.length() << std::endl;
            content = QString("Server : ") + QString::fromStdString(t) + QString("\n");
        }
        else
        {
            content = QString("Client : ") + content + QString("\n");
        }
        ui->textBrowser->append(content);
    }
}

void SocketClient::disconnected()
{
    //updata diconnected info
    QString temp = QString("Connection Status：No Server");
    ui->label_networkStatus->setText(temp);        //Show Connection Info
}
