# LegacyYSM 1.12.2 — modern model compatibility test build

Version: 1.1.9-modern.7. Based on RuiXuqi/LegacyYSM v1.1.9 (0322db5).
The original BSD-3-Clause code license and separate asset licenses still apply.
Upstream notices and authors are retained. This is an independent local fork.

## Controller curves and model defaults (modern.7)

`blend_transition` accepts scalar seconds/Molang expressions and object curves mapping
elapsed seconds to outgoing pose weights. Curves are validated once when the model is
loaded, sorted numerically and sampled with linear interpolation; an omitted zero key
defaults to outgoing weight 1. Runtime blending uses the incoming weight `1 - outgoing`.
The user's Remilia model contains three such 0.3-second curves.

Molang `??` preserves defined zero values and supplies defaults only for missing variables.
Variable presence, like values, is isolated across entities and nested renders. Quoted
string equality preserves case, and unimplemented modern mod-integration string states
use empty idle values. This does not implement those other mods' modern action APIs.
Corrected function mappings include `math.random_integer`, used by this model's random
idle selection. See [REMILIA_FIX.md](REMILIA_FIX.md) for installation and test scope.

## Client-only mode (modern.6)

Servers without YSM automatically use the client's installed model library. Appearance,
textures, favourites, model-defined settings and extra/named animations work locally;
only the local player uses the custom renderer. Client actions send no YSM operation
packets in this mode. Local preferences are persisted separately from server player data
and restored after reconnect, restart and respawn. F3+T or the local model folder screen
reloads packs. Missing/loading model assets temporarily use vanilla rendering.

Forge server support is detected from REGISTER channels belonging to the current
connection, including channels registered before login finishes. Modded peers with YSM
must have matching builds; vanilla and Forge servers without YSM are accepted. Singleplayer
and YSM-enabled servers retain the existing synchronized model library and server checks.
See [CLIENT_MODE.md](CLIENT_MODE.md) for installation and limitations.

## Interface (modern.5)

Shared dark navy panels, rounded buttons and mint selection highlights for the model
library, animation wheel, model settings and material preview. The library adapts its
card columns/rows to the GUI size, searches both display names and IDs, shows the equipped
model and preserves model-auth, favourites and NPC-selection behavior. Compact layouts
keep material preview accessible; the Z shortcut still opens the animation wheel.
The wheel keeps legacy shortcuts and modern group/page navigation, wraps long labels and
shows the hovered action centrally. Windows smaller than 360 GUI pixels wide or 300
GUI pixels high use a compact action-button list with the same navigation and playback. Model-defined range, checkbox and radio controls
preview changes immediately and use the existing validated network/persistence path.
The settings viewport clips drawing and mouse hit tests; scrollable content stays reachable.

Panels use simple vertex fans and borders, with no blur passes, shaders or extra render
targets. Model previews remain the primary rendering cost. This is a UI change, not an
FPS guarantee or a complete redesign of every administrator/settings dialog. No theme
options or resource-pack framework are added.

## Model packages

Reads public `ysm.json` spec 1/2 unencrypted folders and ZIP files (including wrapper
folders) in custom/auth, recursively up to sixteen directory levels. Supports Unicode
source filenames, main/arm Bedrock 1.12.0/1.14.0 geometry (one geometry per file), skin
ordering/default selection, metadata, scaling, preview, GUI textures, categorized/paged model wheels and eight legacy shortcut
aliases, legacy arrow assets and the first modern arrow projectile match. Arbitrary
player animation file entries are merged so controllers can reference extra clips.
Cache/network maps retain ordering. Bone parents, duplicates and cycles are validated.

Modern encrypted `.ysm` containers (YSM >= 1.2.0) are still unsupported. Mesh/binding
extensions and unknown geometry versions are rejected. Both client and server need
this fork. Internal filenames/IDs are normalized; moving a package changes its ID.

## Player animation controllers

Reads and merges `files.player.animation_controllers`. Replaces the named legacy slot
only when the package provides a matching definition; absent slots retain legacy behavior.
Implemented slots: player.main/pre_main/post_main, hold_mainhand/hold_offhand,
pre_hold/post_hold, swing/pre_swing/post_swing, use/pre_use/post_use, armor_feet/legs/
chest/head, pre_parallel_0..7 and parallel_0..7 (all with the `player.` prefix).

Implemented controller data: initial_state (default: default), states, ordered transitions
(first true target; at most one transition per distinct render update), concurrent
weighted animations, state variables with numeric remap_curve, numeric Molang on_entry/
on_exit, and blend_transition in seconds. Transition blending interpolates from the
last outgoing pose. Parallel controllers add rotation/position and multiply scale.
Loop, play-once and hold-on-last-frame clips are sampled separately. Animation finished
queries use the current state. State and variable values are separate per entity's
AnimationData; changing model/reloading assets resets them.

Molang q./v./t. aliases, persistent variable.* values and return statements are supported.
Comparison tokenization, operator precedence, nested conditionals and conditionals with
an omitted false branch (`a ? b`, false returns zero) were fixed. Existing numeric math and
queries remain available. Adds ctrl.death/riptide/sleep/swim/climb/climbing/ladder_up/
ladder_down/ladder_stillness/fly/elytra_fly/swim_stand/attacked/jump/sneak/sneaking/run/
walk/idle. Backported swim/riptide states require their existing optional integrations.

Controller conditions and animation bone channels resolve ctrl.hold/swing/use/armor/ride with two single-quoted
string arguments. Supports explicit `$namespace:id`, existing legacy `:category` matchers,
and `~OreDictionaryName` item filters. Minecraft 1.12 has no modern `#tag` registry;
those filters warn and return false. Bone calls are evaluated for the player currently
being rendered, including scalar scale/visibility channels. This is a limited adapter
for the named numeric conditions, not general string-valued Molang. Unknown controller
expressions warn once and return zero. Unsupported query names still follow the legacy
parser's zero-default behavior.

Limits: no nested controller references, blend_via_shortest_path, animation_time_update,
start/loop delays, particles, command execution, fp.arm/projectile/vehicle/GUI/mod-specific
controller slots, string-valued queries or @ script controllers.
The existing coded head-follow rotation still runs. This is a practical subset, not the
complete Bedrock or latest YSM runtime.

## Function scripts, physics and model configuration

Discovers `.molang` files recursively in `functions` or `files.function_path` in both
folders and ZIPs. Transfers their original text through the existing model cache and
compiles numeric function ASTs on the client. Supports `fn.name(args...)`, indexed
`args[n]`, nested calls (maximum depth 32), comments, brace blocks, lazy conditionals,
return, bounded loop/for_each, break/continue, and the existing numeric math library.
Each invocation has isolated temp.* variables; variable.* persists in the current
player/model animation state. Scripts have an execution budget of 10,000 AST steps.
Missing functions warn once and return zero in animation expressions. @ script
controllers and general string/array/object Molang APIs remain unsupported.

`ysm.second_order(key,input,frequency,damping,response)` implements a damped numeric
second-order response with stable integration. Dynamics are separate per player/model;
repeated bone reads in the same render frame reuse the result. Reloading/changing a
model, rewinding the clock or a long pause resets velocity. Adds query.time_stamp in
world ticks, query.yaw_speed, query.position_delta(axis), query.max_health, ysm.ground_speed2 and local movement
input queries. This port's low-FPS stabilization and remote movement approximation
may differ from current YSM behavior; it is not a bit-for-bit engine replacement.

`extra_animation_classify`, #group/#return navigation, `extra_animation_buttons` and
range/checkbox/radio config_forms are available through Z. Groups longer than eight
entries have pages; named extra animations no longer depend on legacy slot aliases.
Settings use the model's variable names, clamp/quantize ranges, save per player/model
in capability NBT and synchronize through the server. Packets are accepted only for
the sender's selected model and declared setting/animation. Radio choices run the
trusted model's numeric label script, including multiple variable assignments; radio
scripts cannot call model functions or use a world query context in this release.

Honors all_cutout (face culling), render_layers_first and gui_no_lighting in model
previews. Legacy clip timeline scripts now run as well as modern controller scripts;
merge_multiline_expr joins timeline arrays with newlines when requested. Commands and
particle callbacks remain unsupported. Rendering flags are code-integrated; the live
public-model test covers gui_no_lighting, while true all_cutout/render_layers_first
require further visual testing with an appropriate model.

## Sound

Loads referenced Ogg resources from `sounds` or `files.sound_path`, supports Unicode sound
names and standard sound event IDs such as minecraft:block.note.pling. Per-file limit:
4 MiB; total referenced audio limit: 66 MiB. Ogg headers are checked; mono Vorbis is
recommended. Resources are carried in the existing model cache/network protocol and
provided by a persistent in-memory pack that survives F3+T.

Animation sound_effects keyframes (including arrays of events at one timestamp) and
controller state-entry sound_effects play locally using the player's sound category,
position and normal distance attenuation. State entry scripts execute before entry sounds.
Duplicate updates do not replay crossed keyframes. State sounds stop on exit; animation
sounds stop on state change, animation completion or a new loop. Model changes, asset
reloads and leaving the world stop associated sounds. Numeric volume/pitch and a boolean
loop parameter are accepted by event objects. Particle locators are not used for sound.

Not implemented: YSM 2.5.3 ysm.play_sound/stop_sound/stop_all_sounds functions, global
playback contexts/flags, procedural sound parameters, first-person audio-preview controls
or arrow/vehicle sound callbacks. Remote players' sounds are reconstructed locally from
rendered animation states, not synchronized over an additional sound packet protocol.
Controllers advance with rendering, so playback timing for offscreen entities is limited.

## PBR

Spec 2 texture objects load uv/normal/specular images without changing skin-selection
entries. Maps must have the same dimensions. ARGB channels are retained, with flat normal/
zero specular defaults when one map is absent. No conversion between LabPBR and legacy
PTGI packing is attempted; use a shader matching the supplied texture encoding.

Optional OptiFine MultiTex bridge uploads normal/specular images to separate GL textures,
restores the base binding, and uses the 56-byte shader vertex format so OptiFine can compute
tangents/mid-UV for model quads. Both shadersmod.client and net.optifine.shaders names are
recognized. First-person and regular model render paths use the extended format only when
OptiFine reports shaders enabled. Texture reloads recreate all maps; OptiFine's texture
cleanup owns the auxiliary IDs. Without OptiFine, ordinary uv textures still render.

Requires a compatible Minecraft 1.12.2 OptiFine installation and an enabled shader with
normal/specular entity-material support (LabPBR for LabPBR maps). Installing this JAR alone
does not add a shader renderer. OptiFine/shader binaries are not bundled. GUI PBR behavior,
shadows and shader-pack-specific material rules depend on the selected shader.

## Build and verification

Minecraft 1.12.2 Forge, Java 8, MixinBooter >= 8.0 (10.6 bundled). Build the wrapper with
JDK 25; it provisions Azul Java 16 for Jabel and Java 8 for tests. Output is Java 8 bytecode.
Use a separate Forge test instance, install exactly one YSM JAR, copy a model into
config/yes_steve_model/custom and run `/ysm model reload` or re-enter the world.

`examples/controller_sound_pbr` demonstrates state switching, weighted animation,
vanilla/custom mono Vorbis audio, and normal/specular maps on the blue skin. Core model/
skins are the upstream CC0 default assets with original author credits; new demo resources
are CC0. Tests cover parsing, cache roundtrip, state/variable isolation, pose blending,
parallel channels, sound frame deduplication, PBR data and the distributed demo. They do
not substitute for live multiplayer testing. The accompanying runtime report covers an
official Minecraft 1.12.2 client, Forge 14.23.5.2860, MixinBooter 10.6 and OptiFine G5
in an isolated local development session using software OpenGL. NaytoTime's CC BY 4.0
model (made with YSM 2.5.2) was tested unmodified: geometry, texture, walking and crouching
work. Version modern.4 additionally executes its unmodified fn.nt_hair function and
ysm.second_order response, exposes all nine configuration forms, and supports its
category wheel. In-game evidence covers toggling the hat, changing mouth/scale values,
persistence after reconnecting, category navigation and named animation playback. Controller/audio/PBR are tested separately with the supplied demo
because that public model contains no controllers, sound effects or PBR maps.
No actual user model has been supplied, so its full behavior has not been verified.

Public format references:
- https://yesstevemodel.github.io/wiki/struct/
- https://yesstevemodel.github.io/wiki/controller/
- https://yesstevemodel.github.io/wiki/sound/
- https://learn.microsoft.com/en-us/minecraft/creator/reference/content/animationsreference/examples/animationcontroller

The OptiFine bridge targets the public reflection API shapes inspected in the 1.12 source
mirror (not bundled): https://github.com/Awe23123/Optifine-1.12-SRC/tree/master/src/shadersmod/client

Additional references: https://yesstevemodel.github.io/wiki/molang/script/, https://yesstevemodel.github.io/wiki/molang/ref/, https://yesstevemodel.github.io/wiki/roulette/.
