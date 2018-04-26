mod db;

/// ベクトル空間の解像度 (成分を表現する型) を示す符号付き 8bit コード
enum Resolution {
  BIT = 0,
  UINT8 = 1,
  INT8 = -1,
  UINT16 = 2,
  INT16 = -2,
  UINT32 = 3,
  INT32 = -3,
  UINT64 = 4,
  INT64 = -4,
  FLOAT16 = 10,
  FLOAT32 = 11,
  FLOAT64 = 12,
  FLOAT80 = 13,
  FLOAT128 = 14,
}

/// ベクトルデータの圧縮方法を示す符号なし 3bit コード
enum Compression {
  /// 圧縮なし
  UNCOMPRESS = 0x00,
  /// LZ4 圧縮
  LZ4 = 0x01,
  /// Z-Standard 圧縮
  ZSTD = 0x02
}

