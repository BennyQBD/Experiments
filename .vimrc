set nocompatible              " be iMproved, required
filetype off                  " required

" set the runtime path to include Vundle and initialize
set rtp+=~/.vim/bundle/Vundle.vim
call vundle#begin()
" alternatively, pass a path where Vundle should install plugins
"call vundle#begin('~/some/path/here')

" let Vundle manage Vundle, required
Plugin 'gmarik/Vundle.vim'
Plugin 'Valloric/YouCompleteMe'
Plugin 'tikhomirov/vim-glsl'

" All of your Plugins must be added before the following line
call vundle#end()            " required
filetype plugin indent on    " required
" To ignore plugin indent changes, instead use:
"filetype plugin on
"
" Brief help
" :PluginList          - list configured plugins
" :PluginInstall(!)    - install (update) plugins
" :PluginSearch(!) foo - search (or refresh cache first) for foo
" :PluginClean(!)      - confirm (or auto-approve) removal of unused plugins
"
" see :h vundle for more details or wiki for FAQ
" Put your non-Plugin stuff after this line

set tabstop=4
set shiftwidth=4
set smartindent
set number
set mouse=a
command NT NERDTree
map ]b :bn<cr>
map [b :bp<cr>
cd %:p:h
let g:ycm_global_ycm_extra_conf = '~/.ycm_extra_conf.py'
" If you prefer the Omni-Completion tip window to close when a selection is
" " made, these lines close it on movement in insert mode or when leaving
" " insert mode
autocmd CursorMovedI * if pumvisible() == 0|pclose|endif
autocmd InsertLeave * if pumvisible() == 0|pclose|endif

noremap <Leader>p "0p
noremap <Leader>P "0P
vnoremap <Leader>p "0p

command -nargs=0 -bar Update if &modified
                           \|    if empty(bufname('%'))
                           \|        browse confirm write
                           \|    else
                           \|        confirm write
                           \|    endif
noremap <silent> <C-S> :w<CR>

nnoremap <C-F9> :make<CR>
nnoremap <F9> :make<CR>:!./run<CR>
noremap <C-V> "+p
noremap <C-C> "+y
noremap <C-D> yyp
noremap <C-X> "+d
noremap <C-Z> u
noremap <C-Y> <C-R>
noremap <C-A> ggVG
noremap <C-E> <ESC>:
noremap <C-F> /
noremap <2-LeftMouse> a
noremap <S-MiddleMouse> <LeftMouse>g]
noremap <MiddleMouse> <LeftMouse><C-]>
noremap <C-MiddleMouse> <C-T>
nnoremap <Backspace> i<Backspace>
vnoremap <Backspace> di
nnoremap <S-Left> v<Left>
nnoremap <S-Right> v<Right>
nnoremap <S-Up> v<Up>
nnoremap <S-Down> v<Down>

vnoremap <Tab> >gv
vnoremap <S-Tab> <gv
vnoremap <S-C> :s!^!//!<CR>
vnoremap <S-X> :s!^//!!<CR>
vnoremap <S-DOWN> <DOWN>
vnoremap <S-UP> <UP>

inoremap <S-MiddleMouse> <ESC><LeftMouse>g]
inoremap <MiddleMouse> <ESC><LeftMouse><C-]>a
inoremap <C-MiddleMouse> <ESC><C-T>a
inoremap <C-V> <Left><Right><ESC>"+pa
inoremap <C-D> <ESC>yypa
inoremap <C-Z> <ESC>ua
inoremap <C-Y> <ESC><C-R>a
inoremap <C-A> <ESC>ggVG
inoremap <C-E> <ESC>:
inoremap <C-F> <ESC>/
inoremap <C-F9> <ESC>:make<CR>
inoremap <F9> <ESC>:make<CR>:!./run<CR>
inoremap <silent> <C-S> <Left><ESC>:w<CR>i<Right><Right>
inoremap <S-Left> <ESC>v<Left>
inoremap <S-Right> <ESC>v<Right>
inoremap <S-Up> <ESC>v<Up>
inoremap <S-Down> <ESC>v<Down>

autocmd VimEnter * NERDTree
autocmd FileType * setlocal formatoptions-=c formatoptions-=r formatoptions-=o
