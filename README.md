# IMG2ASCII

Converts an image to ASCII art based on a character set of increasing character "weight".

Usage: `java -jar IMAGE2ASCII.jar <options>`

```
img2ascii [--help] [-f=<int>] [-h=<int>] [-o=<ascii-file>]
                 [-r=<gray-ramp>] [-w=<int>] <img-file>
Converts an image to ASCII art.
      <img-file>           Path to image file
      --help               Display this help message
  -f, --font-size=<int>    Font size to optimise size for
  -h, --height=<int>       Max height of ASCII output in chars
  -o, --out=<ascii-file>   Path to output file
  -r, --ramp=<gray-ramp>   Set ASCII char ramp for conversion
                             Default: "@%#*+=-:. "
                             Example: "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. "
  -w, --width=<int>        Max width of ASCII output in chars
```

Program requires Picocli for compilation, pre-compiled JAR-file can be found under releases.
