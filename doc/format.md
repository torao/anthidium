# Data File Format

## Basic

* All signed/unsigned multi-byte integer `INT16`, `INT32` and `INT64` are stored in Little Endian.
* Application data indicated by `JSON` type is actually saved in MessagePack format.

## Common Structure

All integers are encoded in little endian.

### File Header

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

### Variant Integer Type

In this specification, `VARINT` and `VARUINT` are introduced for the purpose to reduce its data-size. This is same encoding as the specification of [Protocol Buffers](https://developers.google.com/protocol-buffers/docs/encoding#varints).

The variant-encoded integers are multi-byte but the restored size MUST NOT exceed the specification length. For example, the value restored from `VARUTINT16` field must be in the range from 0 to 65,535.

このプロトコルにおいて varint を使用する目的は、精度の分からない数値を保持することではなく、偏りのある値を効率よく保存するためである。

signed integer は ZigZag エンコーディングである。
```
if(value < 0) abs(value) * 2 - 1 else value * 2
```
で VARUINT と同様に扱う。


## Data Types

| Notation | Size[B] | Name | Values |
|:---------|-----:|:-----|:-------|
| `BIT`    | 1    | 1 bit | `0` or `1` |
| `INT`   | 1,2,4,8,16 | signed integer | 
| `UINT`   | 1,2,4,8,16 | unsigned integer |
| `VARINT` | 1-3,1-5,1-9 | variant-encoded signed integer |
| `VARUINT` | 1-3,1-5,1-9 | variant-encoded unsigned integer |
| `FLOAT` | 2,4,8,10,16 | floating point |
| `STRING` | * | |
| `JSON` | * | json representation |

### Constants

Type code is present as following `INT8` values:

| TYPE       | Code | Size |
|:-----------|:-----|:-----|
| `BOOL`     | `0`  | 1    |
| `UINT8`    | `1`  | 1    |
| `INT8`     | `-1` | 1    |
| `UINT16`   | `2`  | 2    |
| `INT16`    | `-2` | 2    |
| `UINT32`   | `3`  | 4    |
| `INT32`    | `-3` | 4    |
| `UINT64`   | `4`  | 8    |
| `INT64`    | `-4` | 8    |
| `FLOAT32`  | `11` | 4    |
| `FLOAT64`  | `12` | 8    |
| `STRING`   | `100` | *   |
| `JSON`     | `101` | *   |

Vector compression constant is represent as 3bit

| Code | Compression |
|:---|:----|
| `0b000` | Uncompressed `FLOAT64` Array |
| `0b001` | Snappy Compressed Array |


### Vector Space Information Block

| Field Size | Description | Data Type      | Comments                        |
|:-----------|:------------|:---------------|:--------------------------------|
| 1          | type        | `UINT8`        | Meta-Info Signature `'^'`       |
| 4          | length      | `UINT32`       | Header Size                     |
| *          | rank        | `VARUINT32`    | Rank of Vector Space            |
| *          | dimensions  | `VARUINT32[*]` | Dimension × Rank                |
| 1          | resolution  | `UINT8`        | Resolution Type                 |
| 1          | pack        | `UINT8`        | Pack Method for Vector          |
| 1          | paging      | `UINT8`        | Paging Structure and Method     |
| *          | attributes  | `JSON`         | Application Specified Attribute |

Application Data Type is:

## Blank Entry

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