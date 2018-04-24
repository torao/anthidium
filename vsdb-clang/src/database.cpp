//
// Created by Takami Torao on 2018/04/22.
//
#include "vsdb.h"
#include "core.h"
#include <stdexcept>
#include <sys/stat.h>
#include <sys/unistd.h>
#include <sys/fcntl.h>

namespace vsdb {
    const uint8_t Database::SIGNATURE[2] = {'V', 'S'};
    const uint16_t Database::VERSION = 0x0000;

    Database::Database(const std::string &filename) {
        this->filename = filename;
        this->fd = -1;
    }

    Database::~Database() {
        this->close();
    }

    void Database::open() {
        if (this->fd >= 0) {
            throw std::runtime_error("database already opened");
        }
        this->fd = ::open(filename.c_str(), O_RDWR | O_APPEND | O_BINARY | O_CREAT);
        if (this->fd < 0) {
            throw std::runtime_error("cannot open file: " + this->filename);
        }

        if (this->size() == 0) {
            this->init();
        }
    }

    void Database::close() {
        if (this->fd >= 0) {
            ::close(fd);
            this->fd = -1;
        }
    }

    uint64_t Database::size() const {
        struct stat buf;
        if (::fstat(fd, &buf) != 0) {

        }
        return buf.st_size;
    }

    void Database::init() const {
        Buffer buffer(4);
        buffer.put((uint8_t*)Database::SIGNATURE, 0, 2);
        buffer.put(Database::VERSION);
        buffer.flip();
        ::lseek(fd, 0, SEEK_SET);
        ::write(fd, buffer.array(), buffer.limit());
    }

    void Database::verify() const {
        Buffer buffer(4);
        buffer.put((uint8_t*)Database::SIGNATURE, 0, 2);
        buffer.put(Database::VERSION);
        buffer.flip();
        ::lseek(fd, 0, SEEK_SET);
        ::write(fd, buffer.array(), buffer.limit());
    }

}
