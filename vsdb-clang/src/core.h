//
// Created by Takami Torao on 2018/04/22.
//

#ifndef __VSDB_CORE_H__
#define __VSDB_CORE_H__

#include <cstdint>

namespace vsdb {

    class Buffer final {
    private:
        uint8_t *buffer;
        size_t m_position;
        size_t m_limit;
        size_t m_capacity;
        bool extensible;

        void ensure_capacity(size_t size);

    public:
        Buffer(size_t size, bool extensible = false);

        ~Buffer();

        size_t position() const { return m_position; }

        size_t limit() const { return m_limit; }

        size_t capacity() const { return m_capacity; }

        const uint8_t *array() const { return buffer; }

        Buffer *flip() {
            m_limit = m_position;
            m_position = 0;
            return this;
        }

        Buffer *put(uint8_t b);

        Buffer *put(uint16_t b);

        Buffer *put(uint32_t b);

        Buffer *put(uint64_t b);

        Buffer *put(int8_t b) { return put((uint8_t) b); }

        Buffer *put(int16_t b) { return put((uint16_t) b); }

        Buffer *put(int32_t b) { return put((uint32_t) b); }

        Buffer *put(int64_t b) { return put((uint64_t) b); }

        Buffer *put(uint8_t *b, off_t offset, size_t size);

    };
}

#endif // __VSDB_CORE_H__
