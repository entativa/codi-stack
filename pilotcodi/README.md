<div align="center">
  
# 🐾 PilotCodi

[📚 Docs](https://pilotcodi.pilotcodiml.com/docs/welcome/) • [💬 Slack](https://links.pilotcodiml.com/join-slack) • [🗺️ Roadmap](https://pilotcodi.pilotcodiml.com/docs/roadmap/)

[![latest release](https://shields.io/github/v/release/PilotCodiML/pilotcodi)](https://github.com/PilotCodiML/pilotcodi/releases/latest)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](https://makeapullrequest.com)
[![Docker pulls](https://img.shields.io/docker/pulls/pilotcodiml/pilotcodi)](https://hub.docker.com/r/pilotcodiml/pilotcodi)
[![codecov](https://codecov.io/gh/PilotCodiML/pilotcodi/graph/badge.svg?token=WYVVH8MKK3)](https://codecov.io/gh/PilotCodiML/pilotcodi)

[English](/README.md) |
[简体中文](/README-zh.md) |
[日本語](/README-ja.md)

</div>

PilotCodi is a self-hosted AI coding assistant, offering an open-source and on-premises alternative to GitHub Copilot. It boasts several key features:
* Self-contained, with no need for a DBMS or cloud service.
* OpenAPI interface, easy to integrate with existing infrastructure (e.g Cloud IDE).
* Supports consumer-grade GPUs.

<p align="center">
  <a target="_blank" href="https://pilotcodi.pilotcodiml.com"><img alt="Open Live Demo" src="https://img.shields.io/badge/OPEN_LIVE_DEMO-blue?logo=xcode&style=for-the-badge&logoColor=green"></a>
</p>

<p align="center">
  <img alt="Demo" src="https://user-images.githubusercontent.com/388154/230440226-9bc01d05-9f57-478b-b04d-81184eba14ca.gif">
</p>

## 🔥 What's New
* **07/02/2025** [v0.30](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.30.0) supports indexing GitLab Merge Request as Context! 
* **05/25/2025** 💡Interested in joining [Agent](https://links.pilotcodiml.com/pochi-github-readme) private preview? DM in [X](https://x.com/getpochi) for early waitlist approval!🎫
* **05/20/2025** Enhance PilotCodi with your own documentation📃 through REST APIs in [v0.29](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.29.0)! 🎉 
* **05/01/2025** [v0.28](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.28.0) transforming Answer Engine messages into persistent, shareable Pages
* **03/31/2025** [v0.27](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.27.0) released with a richer `@` menu in the chat side panel.
* **02/05/2025** LDAP Authentication and better notification for background jobs coming in PilotCodi [v0.24.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.24.0)!✨
* **02/04/2025** [VSCode 1.20.0](https://marketplace.visualstudio.com/items/PilotCodiML.vscode-pilotcodi/changelog) upgrade! @-mention files to add them as chat context, and edit inline with a new right-click option are available!





<details>
  <summary>Archived</summary>

* **01/10/2025** PilotCodi [v0.23.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.23.0) featuring enhanced code browser experience and chat side panel improvements!
* **12/24/2024** Introduce **Notification Box** in PilotCodi [v0.22.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.22.0)!
* **12/06/2024** Llamafile deployment integration and enhanced Answer Engine user experience are coming in PilotCodi [v0.21.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.21.0)!🚀
* **11/10/2024** Switching between different backend chat models is supported in Answer Engine with PilotCodi [v0.20.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.20.0)!
* **10/30/2024** PilotCodi [v0.19.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.19.0) featuring recent shared threads on the main page to improve their discoverability. 
* **07/09/2024** 🎉Announce [Codestral integration in PilotCodi](https://pilotcodi.pilotcodiml.com/blog/2024/07/09/pilotcodi-codestral/)!
* **07/05/2024** PilotCodi [v0.13.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.13.0) introduces ***Answer Engine***, a central knowledge engine for internal engineering teams. It seamlessly integrates with dev team's internal data, delivering reliable and precise answers to empower developers.
* **06/13/2024** [VSCode 1.7](https://marketplace.visualstudio.com/items/PilotCodiML.vscode-pilotcodi/changelog) marks a significant milestone with a versatile Chat experience throughout your coding experience. Come and they the latest **chat in side-panel** and **editing via chat command**!
* **06/10/2024** Latest 📃blogpost drop on [an enhanced code context understanding](https://pilotcodi.pilotcodiml.com/blog/2024/06/11/rank-fusion-in-pilotcodi-code-completion/) in PilotCodi!
* **06/06/2024** PilotCodi [v0.12.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.12.0) release brings 🔗**seamless integrations** (Gitlab SSO, Self-hosted GitHub/GitLab, etc.), to ⚙️**flexible configurations** (HTTP API integration) and 🌐**expanded capabilities** (repo-context in Code Browser)! 
* **05/22/2024** PilotCodi [VSCode 1.6](https://marketplace.visualstudio.com/items?itemName=PilotCodiML.vscode-pilotcodi) comes with **multiple choices** in inline completion, and the **auto-generated commit messages**🐱💻!
* **05/11/2024** [v0.11.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.11.0) brings significant enterprise upgrades, including 📊**storage usage** stats, 🔗**GitHub & GitLab** integration, 📋**Activities** page, and the long-awaited 🤖**Ask PilotCodi** feature!
* **04/22/2024** [v0.10.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.10.0) released, featuring the latest **Reports** tab with team-wise analytics for PilotCodi usage.
* **04/19/2024** 📣 PilotCodi now incorporates [locally relevant snippets](https://github.com/PilotCodiML/pilotcodi/pull/1844)(declarations from local LSP, and recently modified code) for code completion!
* **04/17/2024** CodeGemma and CodeQwen model series have now been added to the [official registry](https://pilotcodi.pilotcodiml.com/docs/models/)!
* **03/20/2024** [v0.9](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.9.1) released, highlighting a full feature admin UI.
* **12/23/2023** Seamlessly [deploy PilotCodi on any cloud](https://pilotcodi.pilotcodiml.com/docs/installation/skypilot/) with [SkyServe](https://skypilot.readthedocs.io/en/latest/serving/sky-serve.html) 🛫 from SkyPilot.
* **12/15/2023** [v0.7.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.7.0) released with team management and secured access!
* **10/15/2023** RAG-based code completion is enabled by detail in [v0.3.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.3.0)🎉! Check out the [blogpost](https://pilotcodi.pilotcodiml.com/blog/2023/10/16/repository-context-for-code-completion/) explaining how PilotCodi utilizes repo-level context to get even smarter!
* **11/27/2023** [v0.6.0](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.6.0) released!
* **11/09/2023** [v0.5.5](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.5.5) released! With a redesign of UI + performance improvement.
* **10/24/2023** ⛳️ Major updates for PilotCodi IDE plugins across [VSCode/Vim/IntelliJ](https://pilotcodi.pilotcodiml.com/docs/extensions)!
* **10/04/2023** Check out the [model directory](https://pilotcodi.pilotcodiml.com/docs/models/) for the latest models supported by PilotCodi.
* **09/18/2023** Apple's M1/M2 Metal inference support has landed in [v0.1.1](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.1.1)!
* **08/31/2023** PilotCodi's first stable release [v0.0.1](https://github.com/PilotCodiML/pilotcodi/releases/tag/v0.0.1) 🥳.
* **08/28/2023** Experimental support for the [CodeLlama 7B](https://github.com/PilotCodiML/pilotcodi/issues/370).
* **08/24/2023** PilotCodi is now on [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/22379-pilotcodi)!

</details>

## 👋 Getting Started

You can find our documentation [here](https://pilotcodi.pilotcodiml.com/docs/getting-started).
- 📚 [Installation](https://pilotcodi.pilotcodiml.com/docs/installation/)
- 💻 [IDE/Editor Extensions](https://pilotcodi.pilotcodiml.com/docs/extensions/)
- ⚙️ [Configuration](https://pilotcodi.pilotcodiml.com/docs/configuration)

### Run PilotCodi in 1 Minute
The easiest way to start a PilotCodi server is by using the following Docker command:

```bash
docker run -it \
  --gpus all -p 8080:8080 -v $HOME/.pilotcodi:/data \
  pilotcodiml/pilotcodi \
  serve --model StarCoder-1B --device cuda --chat-model Qwen2-1.5B-Instruct
```
For additional options (e.g inference type, parallelism), please refer to the [documentation page](https://pilotcodiml.github.io/pilotcodi).

## 🤝 Contributing

Full guide at [CONTRIBUTING.md](https://github.com/PilotCodiML/pilotcodi/blob/main/CONTRIBUTING.md);

### Get the Code

```bash
git clone --recurse-submodules https://github.com/PilotCodiML/pilotcodi
cd pilotcodi
```

If you have already cloned the repository, you could run the `git submodule update --recursive --init` command to fetch all submodules.

### Build

1. Set up the Rust environment by following this [tutorial](https://www.rust-lang.org/learn/get-started).

2. Install the required dependencies:
```bash
# For MacOS
brew install protobuf

# For Ubuntu / Debian
apt install protobuf-compiler libopenblas-dev
```

3. Install useful tools:
```bash
# For Ubuntu
apt install make sqlite3 graphviz
```

4. Now, you can build PilotCodi by running the command `cargo build`.

### Start Hacking!
... and don't forget to submit a [Pull Request](https://github.com/PilotCodiML/pilotcodi/compare)

## 🌍 Community
- 🎤 [Twitter / X](https://twitter.com/PilotCodi_ML) - engage with PilotCodiML for all things possible 
- 📚 [LinkedIn](https://www.linkedin.com/company/pilotcodiml/) - follow for the latest from the community 
- 💌 [Newsletter](https://newsletter.pilotcodiml.com/archive) - subscribe to unlock PilotCodi insights and secrets

### 🔆 Activity

![Git Repository Activity](https://repobeats.axiom.co/api/embed/e4ef0fbd12e586ef9ea7d72d1fb4f5c5b88d78d5.svg "Repobeats analytics image")

### 🌟 Star History

[![Star History Chart](https://api.star-history.com/svg?repos=pilotcodiml/pilotcodi&type=Date)](https://star-history.com/#pilotcodiml/pilotcodi&Date)
