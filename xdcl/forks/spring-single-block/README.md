# Fork: one `spring { }` block

**Problem.** The current DSL (and the mainline) has three Spring blocks: `jvmApplication { spring { } }`,
`dependencies { spring { developmentOnly … } }` and `packaging { spring { bootBuildImage … } }`. The latter two
"require `spring { }` on the application", a constraint between properties that XDCL cannot express, so writing
them alone validates fine and fails only later in the reaction.

It also breaks defaults: a settings rule headed by an extension's name (`spring { … }`, "configure Spring wherever
it is enabled") is an error in the mainline, `ambiguous target 'spring' in a defaults rule`, because three
extensions are called `spring`. A fully qualified head (`org.jetbrains.ecosystem.spring.SpringPackaging { … }`) would
work, but nobody wants to write that.

**Fork.** Everything Spring-specific moves into the one block that enables Spring:

```
jvmApplication {
  spring {
    developmentOnly ["org.springframework.boot:spring-boot-docker-compose"]
    bootBuildImage { environment [BP_JVM_CDS_ENABLED: "true"] }
  }
}
```

Schema: [`spring.xdsl`](fork-spring-single-block/src/main/xdcl/spring.xdsl).
Example: [`spring-single-block-example/`](spring-single-block-example). The settings configure `bootBuildImage` for every
Spring application with a `spring { … }` rule, without enabling Spring anywhere.

## Trade-offs

- **For the fork.** "Requires `spring { }`" holds by construction. One place to look for Spring. Defaults headed by
  the extension's name work.
- **For the mainline.** The wireframe's principle that `dependencies` and `packaging` are use-case sections that
  features plug into: all dependencies, Spring's included, are under `dependencies`.
- **General rule this suggests.** A feature should own one block, and an extension name should be unique within a
  plugin. Where a feature has to contribute to a section (`packaging { jib { … } }`), give the contribution its own
  name.
