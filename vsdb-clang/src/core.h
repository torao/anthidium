//
// Created by Takami Torao on 2018/04/22.
//

#ifndef __VSDB_CORE_H__
#define __VSDB_CORE_H__

#include <cstdint>

namespace vsdb {

    extern const std::runtime_error OUT_OF_MEMORY_ERROR;

    /**
     * errno に設定されている値からエラーメッセージを参照します。
     * @return エラーメッセージ
     */
    extern std::string &errno_message();

    /**
     * errno に設定されている値からエラーメッセージを参照します。
     * @return エラーメッセージ
     */
    extern std::string &errno_message();

    class Error : public std::runtime_error {
    public:
        /** エラーコード */
        const char const *code;

        /**
         * エラーコードとエラー発生状況のメッセージを指定して構築します。
         * @param code エラーコード
         * @param message エラーメッセージ
         */
        Error(const char *code, const std::string &message) : std::runtime_error(std::string(code) + ": " + message) {
            this->code = code;
        }
    };

    class IOError : public Error {
    public:
        IOError(const char *code, const std::string &message = "") : Error(code,
                                                                           message.length() == 0 ? errno_message() : (
                                                                                   message + " (" + errno_message() +
                                                                                   ")")) {/* */}
    };

    /**
     * バイナリデータの入出力とメモリ上での操作を行うためのクラスです。
     */
    class Buffer final {
    private:
        uint8_t *buffer;
        size_t m_position;
        size_t m_limit;
        size_t m_capacity;
        bool extensible;

        void ensure_capacity(size_t size);

    public:

        /**
         * 指定されたバイト数のデータが格納できるバッファを構築します。初期状態で入出力位置である position は 0 に、
         * バッファの limit は size の値を持ちますが、実際にメモリ上に確保されたバッファサイズである capacity は size
         * 以上の値を取ります。put() 等による操作が size を超えた時に自動でバッファを拡張する場合には extensible に
         * true を指定します。
         *
         * @param size バッファサイズ
         * @param extensible 自動拡張を行う場合 true
         */
        Buffer(size_t size, bool extensible = false);

        ~Buffer();

        size_t position() const { return m_position; }

        size_t limit() const { return m_limit; }

        size_t capacity() const { return m_capacity; }

        /**
         * このバッファから読み込みまたは書き込み可能な残りのサイズを参照します。
         */
        size_t remaining() const { return m_limit - m_position; }

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

        /**
         * 現在の position から limit までのバッファ領域に指定されたファイルディスクリプタからデータを読み込み、実際に
         * 読み込んだ長さを返します。このメソッドの呼び出しにより実際にバッファに格納されたデータの末尾まで position が
         * 移動します。ファイルから読み込み可能なデータがバッファの格納サイズより少ない場合、読み込み可能なデータを
         * 全て読み込んで remaining より小さな値を返します。
         *
         * @param fd データを読み込むファイルディスクリプタ
         * @return 実際の読み込んだバイト数
         */
        size_t read(int fd);

        /**
         * 現在の position から limit までのバッファ領域に格納されているデータを指定されたファイルディスクリプタへ書き
         * 込みます。このメソッドの呼び出しにより position は実際に書き込みが完了したバッファの末尾まで移動します。
         *
         * @param fd データを書き込むファイルディスクリプタ
         * @return 実際に書き込んだバイト数
         */
        size_t write(int fd);
    };
}

#endif // __VSDB_CORE_H__
