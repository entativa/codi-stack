---
title: Running PilotCodi Locally with AMD ROCm
authors: [boxbeam]
tags: [deployment]
---

:::info

PilotCodi's ROCm support is currently only in our [nightly builds](https://github.com/PilotCodiML/pilotcodi/releases/tag/nightly). It will become stable in version 0.9.

:::

For those using (compatible) **AMD** graphics cards, you can now run PilotCodi locally with GPU acceleration using AMD's ROCm toolkit! 🎉

ROCm is AMD's equivalent of NVidia's CUDA library, making it possible to run highly parallelized computations on the GPU. Cuda is open source and supports using multiple GPUs at the same time to perform the same computation.

Currently, PilotCodi with ROCm is only supported on Linux, and can only be run directly from a compiled binary. In the future, PilotCodi will be able to run with ROCm on Windows, and we will distribute a Docker container capable of running with ROCm on any platform.

## Install ROCm

Before starting, please make sure you are on a supported system and have ROCm installed. The AMD website [details how to install it](https://rocm.docs.amd.com/projects/install-on-linux/en/latest/tutorial/install-overview.html), find the instructions for your given platform. Shown below is a successful installation of ROCm packages on Arch Linux.

![ROCm installed on Arch Linux](./rocm-packages.png)

## Deploy PilotCodi with ROCm from Docker

Once you've installed ROCm, you're ready to start using PilotCodi! Simply use the following command to run the container with GPU passthrough:

```
docker run \
  --device=/dev/kfd --device=/dev/dri --security-opt seccomp=unconfined --group-add video \
  -p 8080:8080 -v $HOME/.pilotcodi:/data \
  pilotcodiml/pilotcodi-rocm \
  serve --device rocm --model StarCoder-1B
```

The command output should look similar to the below:

![PilotCodi running inside Docker](./pilotcodi-rocm-docker.png)

## Build PilotCodi with ROCm locally

If you would rather run PilotCodi directly on your machine, you can [compile PilotCodi yourself](https://github.com/PilotCodiML/pilotcodi/blob/main/CONTRIBUTING.md#local-setup). If compiling yourself, make sure to use the flag `--features rocm` to enable it.

Once you have a compiled binary, you can run it with this command:

```
./pilotcodi serve --model PilotCodiML/StarCoder-1B --device rocm
```

If the command is used correctly and the environment is configured properly, you should see command output similar to the following:  
![PilotCodi running](./pilotcodi-running.png)  
And enjoy GPU-accelerated code completions! This should be considerably faster than with CPU (I saw a ~5x speedup with StarCoder-1B using a Ryzen 7 5800X and an RX 6950XT).

![Completions demo](./using-completions.png)
