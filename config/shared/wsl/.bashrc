# ~/.bashrc: executed by bash(1) for non-login shells.
# Minimale, aufgeräumte Konfiguration für WSL2
#
# WICHTIG: Diese Datei lebt im Repo unter config/shared/wsl/.bashrc
# ~/.bashrc ist ein Symlink auf diese Datei.
# Einmalig einrichten (pro Maschine):
#   ln -sf ~/develop/github/main/config/shared/wsl/.bashrc ~/.bashrc
# Danach synchronisiert git pull/push diese Datei zwischen allen Maschinen.
# ACHTUNG: Manche Installer (sdkman, nvm, conda) ersetzen ~/.bashrc anstatt
# nur anzuhängen — das löscht den Symlink. Danach einfach neu anlegen.

# If not running interactively, don't do anything
case $- in
    *i*) ;;
      *) return;;
esac

# History configuration
HISTCONTROL=ignoreboth
shopt -s histappend
HISTSIZE=1000
HISTFILESIZE=2000

# Check window size after each command
shopt -s checkwinsize

# Make less more friendly for non-text input files
[ -x /usr/bin/lesspipe ] && eval "$(SHELL=/bin/sh lesspipe)"

# Set a fancy prompt
case "$TERM" in
    xterm-color|*-256color) color_prompt=yes;;
esac

if [ "$color_prompt" = yes ]; then
    PS1='${debian_chroot:+($debian_chroot)}\[\033[01;32m\]\u@\h\[\033[00m\]:\[\033[01;34m\]\w\[\033[00m\]\$ '
else
    PS1='${debian_chroot:+($debian_chroot)}\u@\h:\w\$ '
fi
unset color_prompt

# Window title
if [[ $- == *i* ]]; then
  case "$TERM" in
    xterm*|rxvt*|linux|screen*|tmux*)
      printf '\033]0;%s - bash\007' "$(hostname)"
      ;;
  esac
fi

# Enable color support
if [ -x /usr/bin/dircolors ]; then
    test -r ~/.dircolors && eval "$(dircolors -b ~/.dircolors)" || eval "$(dircolors -b)"
fi

# Bash completion
if ! shopt -oq posix; then
  if [ -f /usr/share/bash-completion/bash_completion ]; then
    . /usr/share/bash-completion/bash_completion
  elif [ -f /etc/bash_completion ]; then
    . /etc/bash_completion
  fi
fi

# ═══════════════════════════════════════════════════════════════════
# r-uu project configuration
# ═══════════════════════════════════════════════════════════════════

# - r-uu WSL2/WSLg graphics (fixes IntelliJ rendering artifacts)
export LIBGL_ALWAYS_SOFTWARE=1          # force Mesa software rendering
export MESA_LOADER_DRIVER_OVERRIDE=softpipe  # stable softpipe instead of llvmpipe
export _JAVA_AWT_WM_NONREPARENTING=1    # fixes floating/detached windows in tiling WMs
export GDK_BACKEND=x11                  # force X11 backend (avoid Wayland quirks in WSLg)

# - r-uu java / graalVM
export GRAALVM_HOME="/opt/graalvm-jdk-25"
export JAVA_HOME="$GRAALVM_HOME"
export PATH="$JAVA_HOME/bin:$PATH"

# - r-uu aliases
if [ -f ~/develop/github/main/config/shared/wsl/aliases.sh ]; then
    source ~/develop/github/main/config/shared/wsl/aliases.sh
fi

# - r-uu auto-start docker containers
if service docker status >/dev/null 2>&1; then
    # docker daemon already running
    :
else
    # start docker daemon
    sudo service docker start >/dev/null 2>&1
fi

# - r-uu docker-compose container auto-start (main project)
if command -v docker &> /dev/null; then
    DOCKER_COMPOSE_DIR="/home/r-uu/develop/github/main/config/shared/docker"
    if [ -f "$DOCKER_COMPOSE_DIR/docker-compose.yml" ]; then
        (cd "$DOCKER_COMPOSE_DIR" && docker compose up -d >/dev/null 2>&1 &)
    fi
fi