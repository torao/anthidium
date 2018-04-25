extern crate byteorder;
extern crate fs2;

use self::byteorder::{LittleEndian, WriteBytesExt};
use self::fs2::FileExt;
use std::error::Error;
use std::fs::{File, OpenOptions};
use std::io::{BufReader, BufWriter, Read, SeekFrom};

const FILE_SIGNATURE: [char; 2] = ['V', 'S'];
const FILE_VERSION: u16 = 0x00;

pub struct VectorStorage {
  file_name: String,
  file: File,
}

impl VectorStorage {
  /// 指定されたローカルファイルをオープンしベクトル DB を構築します。
  ///
  pub fn new(file_name: String) -> Result<VectorStorage, String> {

    // フォーマットの検証まはた新規作成のためファイルをオープンしてロック
    OpenOptions::new().read(true).write(true).create(true).open(file_name).map_err(|err|{ Err(err.description()) }).and_then(|file| {
      file.lock_exclusive().map_err(|err|{ Err(err.description()) }).and_then(|()| {
        match file.metadata() {
          Ok(meta) =>
            match meta.len() {
              0 => create(file),   // 新規作成
              length if length >= 4 => verify(file),   // フォーマットの検証
              length =>
                Err(format!("specified file is not vector storage: {}", file_name))
            }
          Err(err) => Err(err.description())
        }
      }).and_then(|_| {
        VectorStorage { file_name, file }
      }).map_err(|err| {
        drop(file);
        err
      })
    })
  }
}

/// 指定されたファイルに初期データを書き込みます。
///
fn create(mut file: File) -> Result<(), String> {
  file.seek(SeekFrom::Start(0)).and_then(|_| {
    let mut out = BufWriter::new(file);
    out.write(&FILE_SIGNATURE).and_then(|_| {
      out.write_u16::<LittleEndian>(FILE_VERSION).and_then(|_| {
        out.flush()
      })
    })
  }).map_err(|error| { Err(error.description()) })
}

/// 指定されたファイルからヘッダを読み込んで確認します。
///
fn verify(file: File) -> Result<(), String> {}
