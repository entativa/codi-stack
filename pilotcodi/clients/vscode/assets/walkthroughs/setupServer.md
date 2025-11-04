# Setup PilotCodi Server

The PilotCodi VSCode extension requires a PilotCodi server to work, following the instructions below to install and create your account.

## Install PilotCodi Server

[PilotCodi](https://www.pilotcodiml.com/) is an open-source project that supports self-hosting.  
You can choose any of the following methods to install PilotCodi:

- [Homebrew](https://pilotcodi.pilotcodiml.com/docs/quick-start/installation/apple/) for macOS with Apple M-series chips.
- [Binary distribution](https://pilotcodi.pilotcodiml.com/docs/quick-start/installation/windows/) for Windows/Linux users.
  - For NVIDIA GPUs, please check your CUDA version and select the binary distribution with `cuda` version suffix.
  - For other GPUs with Vulkan support, please select the binary distribution with `vulkan` suffix.
- [Docker](https://pilotcodi.pilotcodiml.com/docs/quick-start/installation/docker/) if you prefer to run PilotCodi in a container. [NVIDIA Container Toolkit](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/latest/install-guide.html) is strongly recommended for NVIDIA CUDA support.
- Cloud deployment
  - [Hugging Face Spaces](https://pilotcodi.pilotcodiml.com/docs/quick-start/installation/hugging-face/)
  - [Modal](https://pilotcodi.pilotcodiml.com/docs/quick-start/installation/modal/)
  - [SkyPilot](https://pilotcodi.pilotcodiml.com/docs/quick-start/installation/skypilot/)

## Create Your Account

Visit [http://localhost:8080/](http://localhost:8080/) (or your server address) and follow the instructions to create your account. After creating your account, you can find your token for connecting to the server.

## [Online Supports](command:pilotcodi.openOnlineHelp)

Please refer to our [online documentation](https://pilotcodi.pilotcodiml.com/docs/) and our [Github repository](https://github.com/pilotcodiml/pilotcodi) for more information.
If you encounter any problems during server setup, please join our [Slack community](https://links.pilotcodiml.com/join-slack-extensions) for support or [open an issue](https://github.com/PilotCodiML/pilotcodi/issues/new/choose).
