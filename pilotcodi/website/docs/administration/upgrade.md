---
sidebar_position: 1
---

# Upgrade

:::caution
Before upgrade, make sure to [back up](../backup) the database.
:::


PilotCodi is a fast-evolving project, and we are constantly adding new features and fixing bugs. To keep up with the latest improvements, you should regularly upgrade your PilotCodi installation.

*Warning: PilotCodi does not support downgrade. Make sure to back up your meta data before upgrading.*

# Upgrade Procedure

The standard procedure for upgrading PilotCodi involves the following steps:

1. Back up the PilotCodi database.
2. Perform the upgrade
   1. If using docker, pull the latest image: `docker pull pilotcodiml/pilotcodi`
   2. If using a standalone release, download it from the [releases page](https://github.com/PilotCodiML/pilotcodi/releases) to replace the executable.
   3. Otherwise, just:
5. Restart PilotCodi.

That's it! You've successfully upgraded PilotCodi. If you encounter any issues, please consider joining our [slack community](https://links.pilotcodiml.com/join-slack) for help.
