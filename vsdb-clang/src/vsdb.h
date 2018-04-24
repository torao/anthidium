//
// Created by Takami Torao on 2018/04/22.
//

#ifndef __VSDB_H__
#define __VSDB_H__

#include <string>
#include <io.h>

namespace vsdb {

    /**
     * ベクトル空間データベース。
    */
    class Database final {
    private:
        std::string filename;
        int fd;

        /**
         * データベースの初期化。
         */
        void init() const;

        /**
         * データベースの検証。
         */
        void verify() const;

    public:
        explicit Database(const std::string &filename);

        ~Database();

        /**
         * このデータベースをオープンします。
         */
        void open();

        void close();

        uint64_t size() const;

        static const uint8_t SIGNATURE[2];
        static const uint16_t VERSION;
    };

}

#endif //__VSDB_H__
