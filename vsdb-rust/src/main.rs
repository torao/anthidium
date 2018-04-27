
fn f1() -> Result<i8, String> {
  Err(String::from("hoge"))
}

fn f2() -> Result<String, String> {
  let x = match f1() {
    Ok(x) => x,
    Err(x) => return Err(x)
  };
  Ok(format!("{}", x))
}

fn main() {
  match f2() {
    Ok(x) => println!("Ok({})", x),
    Err(x) => println!("Err({})", x)
  }
  println!("Hello, world!");
}
