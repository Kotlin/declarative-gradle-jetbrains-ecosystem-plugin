# Fork: features that can be switched off again

**Decision:** Not adopted: no switch-off mechanism is added to the schema.

**Problem.** In the mainline a feature is an extension (`serialization { }`, `spring { }`): writing its block enables
it. When a settings default enables it (`defaults [ jvmApplication { spring { } } ]`), a project has no way to switch it
off. There is no `null`, and `reset` only exists for lists and maps.

**Two shapes, both in this fork:**

```
// (a) A first-party feature as a `Bool | block` property    // (b) An extension with an `enabled` flag
kotlin {                                                      spring {
  serialization false              // off                       enabled false
  serialization true               // on, defaults           }
  serialization { enabledFormats [:json] }   // on, configured
}
```

Schema: `serialization` in [`common.xdsl`](fork-feature-switches/src/main/xdcl/common.xdsl) (trait `KotlinFeatures`),
`spring` in [`spring.xdsl`](fork-feature-switches/src/main/xdcl/spring.xdsl).
Example: [`feature-switches-example/`](feature-switches-example). Settings enable both for every application, and `ai`
opts out of both.

## Trade-offs

- (a) reads best and gives `serialization true` as a short "on with defaults". But it only works for **first-party**
  features: a property has to be declared by the type's owner, so a third-party plugin (Spring, Ktor, …) cannot
  contribute a feature in this shape. Mixing the two shapes means users must know who owns which feature.
- (b) works for any plugin, but `spring { enabled false }` reads oddly, and the reaction always fires and has to check
  `enabled`.
- The alternative that needs no schema change is to not enable features through ambient defaults at all, and use a
  prefab instead (see [`petklinik-idiomatic`](../petklinik-idiomatic)).
- Composition works across the branches of the union: settings set `serialization { enabledFormats [:json] }`, a
  project writes `serialization { enabledFormats { append [:cbor] } }` and gets `[:json, :cbor]`, while
  `serialization false` replaces the whole value.
