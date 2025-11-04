---
authors: [wwayne]
tags: [deployment]
---

import noInternetImg from './no-internet.png';

# Deploy PilotCodi in Air-Gapped Environment with Docker

<div align="center">
  <img src={noInternetImg} alt="No internet access" style={{ width: 400 }} />
</div>

Are you working in an air-gapped environment, and wondering if you can still deploy PilotCodi? Fear not, because the answer is YES! 🐱📣

## Prerequisite📋

* Docker installed on both the internet-connected computer and the offline computer.

## Offline Deployment Guide🐾

Here's how we'll deploy PilotCodi in an offline environment:

* Create a Docker image on a computer with internet access. 
* Transfer the image to your offline computer.
* Run the Docker image and let PilotCodi work its magic! ✨

Now, let's dive into the detailed steps:

1. Create a new **Dockerfile** on a computer with internet access.

```docker
FROM pilotcodiml/pilotcodi

ENV PILOTCODI_MODEL_CACHE_ROOT=/models

RUN /opt/pilotcodi/bin/pilotcodi-cpu download --model StarCoder-1B
RUN /opt/pilotcodi/bin/pilotcodi-cpu download --model Nomic-Embed-Text
```

The **PILOTCODI_MODEL_CACHE_ROOT** env var sets the directory for saving downloaded models. By setting `ENV PILOTCODI_MODEL_CACHE_ROOT=/models`, we instruct PilotCodi to save the downloaded model files in the `/models` directory within the Docker container during the build process.

2. Build the Docker image which containing the model

```bash
docker build -t pilotcodi-offline .
```

3. Save the Docker image to a tar file:

```bash
docker save -o pilotcodi-offline.tar pilotcodi-offline
```

4. Copy the `pilotcodi-offline.tar` file to the computer without internet access.

5. Load the Docker image from the tar file:

```bash
docker load -i pilotcodi-offline.tar
```

6. Run the PilotCodi container

```bash
docker run -it \
  --gpus all -p 8080:8080 -v $HOME/.pilotcodi:/data \
  pilotcodi-offline \
  serve --model StarCoder-1B --device cuda
```

Once the container is running successfully, you should see the CLI output similar to the screenshot below:

![PilotCodi Cli Output](./cli-output.png)

If you encounter any further issues or have questions, consider join our [slack community](https://links.pilotcodiml.com/join-slack). Our friendly PilotCodi enthusiasts are always ready to lend a helping paw and guide you to the answers you seek! 😸💡
