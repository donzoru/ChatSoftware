#include "des.h"

DES::DES(QObject *parent) : QObject(parent)
{
    //initial
    plain.clear();
    cipher.clear();
    table.clear();
    for(size_t i=0;i<16;++i)
    {
        std::bitset<64> t(i);
        table.push_back(t);
    }
}

DES::~DES()
{


}

std::bitset<32> DES::f(const std::bitset<32> &R, const std::bitset<48> &k)
{
    std::bitset<48> expandR;

    // Extand Permutation
    for(int i=0; i<48; ++i)
        expandR[47-i] = R[32-E[i]];

    expandR = expandR ^ k;

    // S_BOX Permutation
    std::bitset<32> output;
    int x = 0;
    for(int i=0; i<48; i=i+6)
    {
        int row = expandR[47-i]*2 + expandR[47-i-5];
        int col = expandR[47-i-1]*8 + expandR[47-i-2]*4 + expandR[47-i-3]*2 + expandR[47-i-4];
        int num = S_BOX[i/6][row][col];
        std::bitset<4> binary(num);
        output[31-x] = binary[3];
        output[31-x-1] = binary[2];
        output[31-x-2] = binary[1];
        output[31-x-3] = binary[0];
        x += 4;
    }
    // p Permutation
    std::bitset<32> tmp = output;
    for(int i=0; i<32; ++i)
        output[31-i] = tmp[32-P[i]];
    return output;
}

std::bitset<28> DES::leftShift(std::bitset<28> & k, const int &shift)
{
    //left shift key
    std::bitset<28> tmp = k;
    for(int i=27; i>=0; --i)
    {
        if(i-shift<0)
            k[i] = tmp[i-shift+28];
        else
            k[i] = tmp[i-shift];
    }
    return k;
}

std::bitset<64> DES::charsToBitset(const std::string &s)
{
    // convert string which saves original text to bitset
    std::bitset<64> bits;
    //std::cout<< s.length() << "  ";
    bits.reset();
    for(size_t i=0; i<8; ++i)
    {
        for(size_t j=0; j<8; ++j)
        {
            bits[i*8+j] = ((s[i]>>j) & 1);
        }
    }
    //std::cout<< bits << std::endl;
    return bits;
}

std::bitset<64> DES::stringToBitset(std::string &s)
{
    //convert hex string to bitset
    std::bitset<64> res;
    res.reset();
    for(size_t i=0;i<s.length();++i)
    {
        size_t t = 0;
        if(s[i]>='A' && s[i]<='F')
        {
            t = s[i] - 'A' + 10;
        }
        else
        {
            t = s[i] - '0';
        }
        res |= (table[t]<<(( 15 - i ) * 4));
    }
    return res;
}

void DES::generateKeys()
{
    std::bitset<56> realKey;
    std::bitset<28> left;
    std::bitset<28> right;
    std::bitset<48> compressKey;
    // convert 64 bits key to 56 bits
    for (int i=0; i<56; ++i)
        realKey[55-i] = key[64 - PC_1[i]];

    // generate subkeys, save to vector subKeys
    for(int round=0; round<16; ++round)
    {
        // left 28 bits and right 28bits
        for(int i=28; i<56; ++i)
            left[i-28] = realKey[i];
        for(int i=0; i<28; ++i)
            right[i] = realKey[i];

        // left shift
        left = leftShift(left, shiftBits[round]);
        right = leftShift(right, shiftBits[round]);

        // convert 56 bits to 48 bits
        for(int i=28; i<56; ++i)
            realKey[i] = left[i-28];
        for(int i=0; i<28; ++i)
            realKey[i] = right[i];
        for(int i=0; i<48; ++i)
            compressKey[47-i] = realKey[56 - PC_2[i]];
        subKey[round] = compressKey;
    }
}

void DES::addPlain(const std::bitset<64> &newPlain)
{
    if(plain.size()!=cipher.size()){
        plain.clear();
        cipher.clear();
    }
    plain.push_back(newPlain);
    cipher.push_back(newPlain);
}

void DES::addCipher(const std::bitset<64> &newCipher)
{
    if(plain.size()!=cipher.size())
    {
        plain.clear();
        cipher.clear();
    }
    plain.push_back(newCipher);
    cipher.push_back(newCipher);
}

void DES::setValue(const int &mode,
                   const QString &content,
                   const QString &password)
{
    plain.clear();
    cipher.clear();
    //std::cout << a.toStdString() << " " << b.toStdString() << std::endl;

    std::string newContent = content.toStdString();
    std::string newPassword = password.toStdString();
    newPassword.resize(8);

    key = charsToBitset(newPassword);
    std::string t;
    if(mode == 1)//encrypt mode
    {
        for(size_t i=0;i<newContent.length();i+=8)
        {
            //std::cout<<i<<std::endl;
            t = newContent.substr(i,8);
            //cout<<t<<endl;
            t.resize(8);
            plain.push_back(charsToBitset(t));
            cipher.push_back(plain.back());
        }
    }
    else//decrypt mode
    {
        for(size_t i=0;i<newContent.length();i+=16)
        {
            t = newContent.substr(i,16);
            t.resize(16);
            cipher.push_back(stringToBitset(t));
            plain.push_back(cipher.back());
        }
    }
    /*
    for(size_t i=0;i<plain.size();++i)
        cout<<plain[i]<<endl;
    */
    //std::cout<<plain.size()<<std::endl;

}

std::string DES::bitsetToHexString(std::bitset<64> &bits)
{
    //convert bitset to hex in standart steing
    std::stringstream res;
    res << std::hex << std::uppercase << bits.to_ullong();
    //std::cout<<bits<<std::endl;
    return res.str();
}

std::string DES::bitsetToCharString(std::bitset<64> &bits)
{
    //convert bitset to standard string per byte
    std::string res="";
    //std::cout<<bits<<std::endl;
    for(size_t i=0;i<63;i+=8)
    {
        std::bitset<8> t;
        t.reset();
        for(size_t j=0;j<8;++j)
            t[j] = bits[i+j];

        res += (char)(t.to_ulong());
    }
    return res;
}

QString DES::encrypt()
{
    std::bitset<64> currentBits;
    std::bitset<32> left;
    std::bitset<32> right;
    std::bitset<32> newLeft;

    for(size_t i=0;i<plain.size();++i)
    {
        //std::cout<< i << " " << plain[i] << std::endl;
        currentBits.reset();
        left.reset();
        right.reset();
        newLeft.reset();

        // IP
        for(int j=0; j<64; ++j)
            currentBits[63-j] = plain[i][64-IP[j]];

        // get array left and array right
        for(int j=32; j<64; ++j)
            left[j-32] = currentBits[j];
        for(int j=0; j<32; ++j)
            right[j] = currentBits[j];

        // 16 rounds solve with subkey
        for(int round=0; round<16; ++round)
        {
            newLeft = right;
            right = left ^ f(right,subKey[round]);
            left = newLeft;
        }

        // merge the array left and the array right
        for(int j=0; j<32; ++j)
            cipher[i][j] = left[j];
        for(int j=32; j<64; ++j)
            cipher[i][j] = right[j-32];

        // FP
        currentBits = cipher[i];
        for(int j=0; j<64; ++j)
            cipher[i][63-j] = currentBits[64-FP[j]];
        //std::cout<< i << std::endl;
        //std::cout<< cipher[i] << std::endl;
    }
    // return encrypted text
    QString finalResult = "";

    for(size_t i=0;i<cipher.size();++i)
    {
        std::string t = bitsetToHexString( cipher[i] );
        QString tmp = QString::fromStdString( t );
        finalResult += tmp;
    }
    return finalResult;
}

QString DES::decrypt()
{
    std::bitset<64> currentBits;
    std::bitset<32> left;
    std::bitset<32> right;
    std::bitset<32> newLeft;

    //std::cout<< "decrypt" << std::endl;
    for(size_t i=0;i<cipher.size();++i)
    {
        //std::cout<< i << "  " << std::hex << cipher[i].to_ullong() << std::endl;

        // IP
        for(int j=0; j<64; ++j)
            currentBits[63-j] = cipher[i][64-IP[j]];

        // get array left and array right
        for(int j=32; j<64; ++j)
            left[j-32] = currentBits[j];
        for(int j=0; j<32; ++j)
            right[j] = currentBits[j];

        // 16 rounds solve with subkey
        for(int round=0; round<16; ++round)
        {
            newLeft = right;
            right = left ^ f(right,subKey[15-round]);
            left = newLeft;
        }

        // merge the array left and the array right
        for(int j=0; j<32; ++j)
            plain[i][j] = left[j];
        for(int j=32; j<64; ++j)
            plain[i][j] = right[j-32];

        // FP
        currentBits = plain[i];
        for(int j=0; j<64; ++j)
            plain[i][63-j] = currentBits[64-FP[j]];
        //std::cout<< i << std::endl;
    }

    // return original text
    QString finalResult = "";

    for(size_t i=0;i<plain.size();++i)
    {
        std::string t = bitsetToCharString( plain[i] );
        //std::cout<< i << " " <<  plain[i] << " " << t << std::endl;
        QString tmp = QString::fromStdString( t );
        finalResult += tmp;
    }

    return finalResult;
}

