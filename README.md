# Matrix Rain
scala version of https://github.com/nojvek/matrix-rain

## Usage
```
sbt run [-d {h,v}]
        [-c {green,red,blue,yellow,magenta,cyan,white}]
        [-k {ascii,binary,braille,emoji,katakana}] 
        
The famous Matrix rain effect of falling green characters as a cli command

Optional arguments:
  -h, --help            Show this help message and exit.
  -d, --direction {h,v}
                        Change direction of rain. h=horizontal, v=vertical
  -c , --color {green,red,blue,yellow,magenta,cyan,white}
                        Rain color. NOTE: droplet start is always white
  -k, --char-range {ascii,binary,braille,emoji,katakana}
                        Use rain characters from char-range
```

## Screenshots

![Vertical Matrix](./v.gif)

![Horizontal Matrix](./h.gif)