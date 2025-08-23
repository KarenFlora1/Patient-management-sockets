
$files = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object {
    $_.FullName 
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$sw = New-Object System.IO.StreamWriter("sources.txt", $false, $utf8NoBom)
foreach ($f in $files) { $sw.WriteLine($f) }
$sw.Close()


javac -cp "lib/*" -d build -sourcepath src "@sources.txt"