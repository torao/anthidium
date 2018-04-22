//
// Created by Takami Torao on 2018/04/22.
//
#ifndef VSDB_BLOCK_H
#define VSDB_BLOCK_H

#include <cstdint>

class Block {

private:

    /*
     * このブロックのタイプ。
     */
    uint8_t type = 0xFF;

    /*
     * このブロックの長さ。
     */
    uint32_t length = 0;

public:

    Block(){
    }

    ~Block(){
    }

};

#endif //VSDB_BLOCK_H
