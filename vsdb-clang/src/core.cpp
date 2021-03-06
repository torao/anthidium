//
// Created by Takami Torao on 2018/04/22.
//

#include <string>
#include <functional>
#include <iostream>
#include <sys/unistd.h>
#include "core.h"

namespace vsdb {

    const std::runtime_error OUT_OF_MEMORY_ERROR("out of memory");

    /** 内部的に使用するエラーメッセージ領域 */
    thread_local std::string error_message;

    static const uint32_t _bom = 1;

    /**
     * 実行環境のバイトオーダーがリトルエンディアンの場合に true を返す関数。
     *
     * @return リトルエンディアンの場合 true
     */
    constexpr bool little_endian() {
        return *reinterpret_cast<const uint8_t *>(&_bom) == 1;
    }

    static void (*write_uint16)(uint8_t *, uint16_t) = little_endian() ? [](uint8_t *buffer, uint16_t value) {
        *reinterpret_cast<uint16_t *>(buffer) = value;
    } : [](uint8_t *out, uint16_t value) {
        auto in = reinterpret_cast<uint8_t *>(&value);
        out[0] = in[1];
        out[1] = in[0];
    };

    static void (*write_uint32)(uint8_t *, uint32_t) = little_endian() ? [](uint8_t *buffer, uint32_t value) {
        *reinterpret_cast<uint32_t *>(buffer) = value;
    } : [](uint8_t *out, uint32_t value) {
        auto in = reinterpret_cast<uint8_t *>(&value);
        out[0] = in[3];
        out[1] = in[2];
        out[2] = in[1];
        out[3] = in[0];
    };

    static void (*write_uint64)(uint8_t *, uint64_t) = little_endian() ? [](uint8_t *buffer, uint64_t value) {
        *reinterpret_cast<uint64_t *>(buffer) = value;
    } : [](uint8_t *out, uint64_t value) {
        auto in = reinterpret_cast<uint8_t *>(&value);
        out[0] = in[7];
        out[1] = in[6];
        out[2] = in[5];
        out[3] = in[4];
        out[4] = in[3];
        out[5] = in[2];
        out[6] = in[1];
        out[7] = in[0];
    };

    Buffer::Buffer(size_t size, bool extensible) {
        this->buffer = (uint8_t *) malloc(size);
        this->m_position = 0;
        this->m_limit = size;
        this->m_capacity = size;
        this->extensible = extensible;
    }

    Buffer::~Buffer() {
        free(this->buffer);
    }

    void Buffer::ensure_capacity(size_t size) {
        if (m_position + size > m_limit) {
            if (!extensible) {
                throw std::runtime_error("data exceeds the buffer limit");
            } else {
                if (m_position + size > m_capacity) {
                    size_t len = m_capacity + size + std::min(m_capacity * 2, 8 * 1024);
                    buffer = (uint8_t *) realloc(buffer, len);
                    if (buffer == nullptr) {
                        throw OUT_OF_MEMORY_ERROR;
                    }
                    m_capacity = len;
                }
                m_limit = m_position + size;
            }
        }
    }

    Buffer *Buffer::put(uint8_t b) {
        ensure_capacity(1);
        buffer[m_position] = b;
        m_position++;
    }

    Buffer *Buffer::put(uint16_t b) {
        ensure_capacity(2);
        write_uint16(buffer + m_position, b);
        m_position += 2;
    }

    Buffer *Buffer::put(uint32_t b) {
        ensure_capacity(4);
        write_uint32(buffer + m_position, b);
        m_position += 4;
    }

    Buffer *Buffer::put(uint64_t b) {
        ensure_capacity(8);
        write_uint64(buffer + m_position, b);
        m_position += 8;
    }

    Buffer *Buffer::put(uint8_t *b, off_t offset, size_t size) {
        ensure_capacity(size);
        for (size_t i = 0; i < size; i++) {
            buffer[m_position + i] = b[offset + i];
        }
        m_position += size;
    }

    size_t Buffer::read(int fd) {
        size_t size = remaining();
        if (size > 0) {
            size_t len = ::read(fd, buffer + m_position, size);
            m_position += len;
            return len;
        }
        return size;
    }

    size_t Buffer::write(int fd){
        size_t size = remaining();
        if(size > 0){
            size_t len = ::write(fd, buffer + m_position, size);
            if(len < 0){

            }
            m_position += len;
            return len;
        }
        return size;
    }

}
