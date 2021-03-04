module.exports = {
  title: 'Wonderland DoorKnob Docs',
  tagline: 'System design and docs for Wonderland DoorKnob',
  url: 'https://alices-wonderland.github.io',
  baseUrl: '/doorknob/docs/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'alices-wonderland',
  projectName: 'doorknob.alices-wonderland.github.io',
  themeConfig: {
    navbar: {
      title: 'Wonderland DoorKnob',
      logo: {
        alt: 'Wonderland DoorKnob Logo',
        src: 'img/logo.svg',
      },
      items: [
        {
          href: 'https://github.com/alices-wonderland/doorknob',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      copyright: `Copyright © ${new Date().getFullYear()} Wonderland DoorKnob, Inc. Built with Docusaurus.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          routeBasePath: '/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
