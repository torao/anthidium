# データファイルフォーマット

## 基本構造

Vector Space Database は単一ファイルデータベースである。ジャーナルや中間ファイルとして別のファイルを使用する可能性もあるが、マスターのファイルのみを保全すれば整合性は

### データ型

このフォーマット仕様で説明するデータ型は以下の通り:

| Notation  |     Size[B] | Name                     | Values |
|:----------|------------:|:-------------------------|:-------|
| `BIT`     |         1/8 | 1 bit (packed in a byte) | `0` or `1` |
| `INT`     |  1,2,4,8,16 | signed integer           | 
| `UINT`    |  1,2,4,8,16 | unsigned integer         |
| `VARINT`  | 1-3,1-5,1-9 | variant-encoded signed integer |
| `VARUINT` | 1-3,1-5,1-9 | variant-encoded unsigned integer |
| `FLOAT`   | 2,4,8,10,16 | floating point           |
| `JSON`    |           * | json representation      |

全ての符号付きおよび符号なしマルチバイト整数はリトルエンディアンの配置で保存しなければならない。

`JSON` 型で示されるアプリケーションデータは、続くバイナリデータの長さを示す `VARUINT32` 型と MessagePack でフォーマットされた JSON 構造のバイナリである。

| Notation    | Size[B] | Name                        |
|:------------|--------:|:----------------------------|
| `VARUINT32` |     1-5 | length                      |
| `UINT8[*]`  |       * | msgpack-encoded json binary |

The data type used for vector space can be selected from `BIT`, `INT*`, `UINT*`, `FLOAT*`.
We use the type `VTYPE` as vector space and call it space resolution.

### Variant Integer Type

This specification introduce variable length integers `VARINT`. The purpose to introduce `VARINT` in this protocol isn't to store precision-indeterminate numerical values but to save values with deviation efficiently. The encoding of `VARINT` is the same as the Protocol Buffers specification.

In this specification, `VARINT` and `VARUINT` are introduced for the purpose to reduce its data-size. This is same encoding as the specification of [Protocol Buffers](https://developers.google.com/protocol-buffers/docs/encoding#varints).

The variant-encoded integers are multi-byte but the restored size MUST NOT exceed the specification length. For example, the value restored from `VARUTINT16` field must be in the range from 0 to 65,535.

The `VARINT` value is treated the same as `VARUINT` after performing the following ZigZag encoding.

```
if(value < 0) abs(value) * 2 - 1 else value * 2
```

## File Header

The first 4 bytes of data file must begin 2-byte file-signature followed by file-format-version.

| Type       | Description                   | Example  |
|:-----------|:------------------------------|:---------|
| `UINT8[2]` | File Signature (Magic Number) | `'VS'`   |
| `UINT16`   | File Format Version           | `0x0000` |

All implementations MUST acquire exclusive lock on this 4-bytes when it writes data in, and shared lock when read.

### Block Structure

| Field Size | Description | Data Type | Comments |
|:-----------|:------------|:----------|:---------|
| 1          | type        | `UINT8`   | Block signature |
| 4          | length      | `UINT32`  | Length of payload in number of bytes |
| *          | payload     | `UINT8[]` | The actual block data depends on signature |

This data format commonly uses TLV (Type-Length-Value) structure for all data-block. Therefore, your application can safely skip any block that be not able to recognize block-signature, or padding additional data field.

The block-signature is 1-byte code. Typically US-ASCII code that identify block characteristic. The length field doesn't contain the signature and length field size.


![Block](img/block.png)

## File Header

The data-file must begin 2-byte file-signature followed by file-format-version.

| Type       | Description                   | Example  |
|:-----------|:------------------------------|:---------|
| `UINT8[2]` | File Signature (Magic Number) | `'VS'`   |
| `UINT16`   | File Format Version           | `0x0000` |

All implementations MUST acquire exclusive lock on this 4-bytes when it writes data in.

![File Header](img/file-header.png)

## Vector Space Meta-Information Block

| Field Size | Description | Data Type      | Comments                        |
|:-----------|:------------|:---------------|:--------------------------------|
| 1          | type        | `UINT8`        | Meta-Info Signature `'^'`       |
| 4          | length      | `UINT32`       | Header Size                     |
| *          | rank        | `VARUINT32`    | Rank of Vector Space            |
| *          | dimensions  | `VARUINT32[*]` | Dimension × Rank               |
| 1          | resolution  | `INT8`         | Resolution Type                 |
| 1          | compression | `UINT8`        | Compression Method for Vector   |
| 1          | indexing    | `UINT8`        | Indexing Structure and Method   |
| *          | attributes  | `JSON`         | Application Specified Attribute |

A static type is assigned as resolution of the individual vector space. The type identifier is embedded in the header block, and the following values are assigned to each type.
Type code is present as following `INT8` values:

resolution は空間解像度としてベクトル空間のそれぞれに割り当てられた単一の数値型を表す値である。

この仕様に従う実装は `FLOAT32`, `FLOAT64` を実装しなければならない。
それ以外の
`FLOAT16`, `FLOAT80`, `FLOAT128` は実装依存である。

| TYPE       | Code | Size of Element |
|:-----------|:-----|:-----|
| `BIT`      | `0`  | 1/8  |
| `UINT8`    | `1`  | 1    |
| `INT8`     | `-1` | 1    |
| `UINT16`   | `2`  | 2    |
| `INT16`    | `-2` | 2    |
| `UINT32`   | `3`  | 4    |
| `INT32`    | `-3` | 4    |
| `UINT64`   | `4`  | 8    |
| `INT64`    | `-4` | 8    |
| `FLOAT16`  | `10` | 2    |
| `FLOAT32`  | `11` | 4    |
| `FLOAT64`  | `12` | 8    |
| `FLOAT80`  | `13` | 10   |
| `FLOAT128` | `14` | 16   |

ユーザはストレージに保存されるベクトル値 (数値配列) の圧縮方法を指定することができる。ただし `FLOAT` 解像度空間でのベクトル値圧縮は効果がないどころかサイズが増加することがある。

圧縮方法のコードは符号なし 3bit 整数で表される。

| Code    | Compression  |
|:--------|:-------------|
| `0b000` | Uncompressed |
| `0b001` | LZ4          |
| `0b010` | ZStandard    |

## Blank Block

| TYPE     | Description | Example |
|:---------|:------------|:--------|
| `ASCII`  | Entry Type  | `'0'`   |
| `UINT32` | Entry Size  | `0`     |

If delete specific entry, you can overwrite Entry Type to `'0'`.

## Point Entry

| TYPE        | Description                   | Example |
|:------------|:------------------------------|:--------|
| `ASCII`     | Entry Type | `'P'`  |
| `UINT32`    | Entry Size           | `0x01`  |
| `UINT8`     | Option | `0x00` |
| *           | Application Data for This Entry |         |
| `VECTOR` | Vector Space Attribute        | `{...}` |

`0b000`: uncompressed
`0b001`: snappy

### Vector Format

| TYPE        | Description                   | Example |
|:------------|:------------------------------|:--------|
| `UINT8`     | Option | `0x00` |
| `UINT32` | Vector Binary Length | |
| `BYTE[]` | Compressed `FLOAT64` Binary | `{...}` |

`option & 0x07` indicates compression type,
`0b000`: uncompressed
`0b001`: gzip compressed
`0b010`: snappy compressed

## Terminal Entry

| TYPE     | Description | Example |
|:---------|:------------|:--------|
| `ASCII`  | Entry Type  | `'$'`   |

Note that the terminal entry doesn't have entry size. All data following terminal block MUST be ignored.