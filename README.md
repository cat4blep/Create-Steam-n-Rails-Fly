  <h1>Create: Steam 'n' Rails — Create Fly port</h1>
  <p>Steam 'n' Rails for Fabric, Minecraft 26.2, and Create Fly.</p>

[![discord-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/cozy/social/discord-plural_vector.svg)](https://discord.gg/r75NPJWD68) 
</div>

This repository ports [Create: Steam 'n' Rails](https://github.com/Layers-of-Railways/Railway) to [Create Fly](https://github.com/ZurrTum/Create-Fly) for stable Minecraft 26.2. Steam 'n' Rails expands Create's train and steam systems with custom tracks, semaphores, conductors, bogeys, palettes, and other railway content.

The current stable release is **SNR.FLY-STABLE-1.2.1** for Fabric / Minecraft 26.2
## Compatibility

| Component | Required version |
| --- | --- |
| Minecraft | `26.2` exactly |
| Mod loader | Fabric Loader `0.19.3` or newer |
| Fabric API | `0.152.0+26.2` or newer |
| Create | [Create Fly](https://github.com/ZurrTum/Create-Fly) `>=6.0.9-1 <6.0.10-0` |
| Java | 25 |
| This port | `SNR.FLY-STABLE-1.2.1` |

This is a Fabric-only port. Forge and NeoForge are not supported. Use Create Fly rather than another Create implementation, and do not install the original Steam 'n' Rails JAR alongside this port.

## Issue reports

When [reporting a problem](https://github.com/cat4blep/Create-Steam-n-Rails-Fly/issues), include the complete `latest.log` or crash report, mod versions, a short reproduction sequence, and whether it also occurs with only Steam 'n' Rails and its required dependencies installed. For rendering or atlas issues, also include the GPU, driver version, graphics backend (OpenGL/Vulkan), resource packs, mipmap level and anisotropic filtering setting.

## Contributing

Open an issue before starting a large change so its scope can be agreed on and duplicate work can be avoided. Pull requests should describe the affected Minecraft/Create Fly versions and include the checks used to verify the change.

Branches for this port must be named **exactly** `<modloader>-<version>`, with no additional words or suffixes. The current branch is `fabric-26.2`.

Translations for the upstream project are managed through [Crowdin](https://crowdin.com/project/create-steam-n-rails-official). Translation questions can be asked in the [translator's chat](https://discord.com/channels/706277846389227612/1049156352553000970) on the Steam 'n' Rails Discord.

## License

Steam 'n' Rails is licensed under the LGPL license. See [LICENSE](LICENSE) for details.

Certain sections of the code are derived from projects with compatible licenses, including Create (MIT), Quilt Standard Libraries (Apache-2.0), SecurityCraft (MIT), Neruina (MIT), and FramedBlocks (LGPL). Their copyright and license notices remain applicable to the corresponding code.

## Credits

- [Layers of Railways](https://github.com/Layers-of-Railways/Railway) and its contributors for Create: Steam 'n' Rails.
- [ZurrTum/Create-Fly](https://github.com/ZurrTum/Create-Fly) and its contributors for the Create Fly port used by Minecraft 26.2.
- [chaevsfe/Create-Steam-n-Rails-Fly](https://github.com/chaevsfe/Create-Steam-n-Rails-Fly) for fixes adapted into release 1.2.
- The original Create team and all projects credited in the source and license notices.
