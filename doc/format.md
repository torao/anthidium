# Data File Format

## Basic

* All signed/unsigned multi-byte integer `INT16`, `INT32` and `INT64` are stored in Little Endian.
* Application data indicated by `JSON` type is actually saved in MessagePack format.

## Data Types

The data types described in this format specification are as follows:

| Notation | Size[B] | Name | Values |
|:---------|-----:|:-----|:-------|
| `BIT`    | 1/8  | 1 bit (packed in a byte) | `0` or `1` |
| `INT`   | 1,2,4,8,16 | signed integer | 
| `UINT`   | 1,2,4,8,16 | unsigned integer |
| `VARINT` | 1-3,1-5,1-9 | variant-encoded signed integer |
| `VARUINT` | 1-3,1-5,1-9 | variant-encoded unsigned integer |
| `FLOAT` | 2,4,8,10,16 | floating point |
| `STRING` | * | |
| `JSON` | * | MessagePack-encoded JSON |

### Variant Integer

This specification introduce variable length integers `VARINT`. The purpose to introduce `VARINT` in this protocol isn't to store precision-indeterminate numerical values but to save values with deviation efficiently. The encoding of `VARINT` is the same as the Protocol Buffers specification.

| | |
|:---|:---|
| 00 - 7F | | 
| 81 00 - FF 7F |

The variant-encoded integers are multi-byte but the restored size MUST NOT exceed the specification length. For example, the value restored from `VARUTINT16` field must be in the range from 0 to 65,535.

The `VARIN` valus is treated the same as `VARUINT` after performing the following ZigZag encoding.

```
if(value < 0) abs(value) * 2 - 1 else value * 2
```

### Block Structure

This data format uses TLV (Type-Length-Value) structure data-block. Therefore, your application can safely skip any block that have unrecognized block-signature, or padding additional data field.

| Type        | Description     |
|:------------|:----------------|
| `UINT8`     | Block Signature |
| `VARUINT64` | Data Size       |
| *           | Block Data      |

The block-signature is 1-byte code. Typically US-ASCII code that identify block characteristic. The data-size doesn't contain signature and data-size field length.

![Block](img/block.png)

## File Header

The data-file must begin 2-byte file-signature followed by file-format-version.

| Type       | Description                   | Example  |
|:-----------|:------------------------------|:---------|
| `UINT8[2]` | File Signature (Magic Number) | `'VS'`   |
| `UINT16`   | File Format Version           | `0x0000` |

All implementations MUST acquire exclusive lock on this 4-bytes when it writes data in.

![File Header](img/file-header.png)

### Vector Space Meta-Information Block

| Type           | Description                     | Example |
|:---------------|:--------------------------------|:--------|
| `UINT8`        | Meta-Info Signature             | `'^'`   |
| `VARUINT64`    | Header Size                     |         |
| `VARUINT32`    | Rank of Vector Space            |         |
| `VARUINT32[*]` | Dimension × Rank               |         |
| `UINT8`        | Type of Vector Elements         | `100`   |
| `UINT8`        | Pack Method for Vector          |         |
| `UINT8`        | Paging Structure and Method     |         |
| `JSON`         | Application-Specified Attribute | `{...}` |

A static type is assigned as resolution of the individual vector space. The type identifier is embedded in the header block, and the following values are assigned to each type.
Type code is present as following `INT8` values:

| TYPE       | Code | Size of Element |
|:-----------|:-----|:-----|
| `BOOL`     | `0`  | 1/8  |
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