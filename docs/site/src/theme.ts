import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#6D28D9' },
    secondary: { main: '#0F172A' },
    background: { default: '#F8FAFC', paper: '#FFFFFF' },
    text: { primary: '#0F172A', secondary: '#475569' },
    divider: '#E2E8F0',
  },
  shape: { borderRadius: 10 },
  typography: {
    fontFamily: '"IBM Plex Sans", system-ui, sans-serif',
    h1: { fontSize: '2.25rem', fontWeight: 700, letterSpacing: '-0.02em' },
    h2: { fontSize: '1.75rem', fontWeight: 700, letterSpacing: '-0.01em' },
    h3: { fontSize: '1.15rem', fontWeight: 600 },
    body1: { lineHeight: 1.75 },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: { scrollBehavior: 'smooth' },
        pre: { margin: 0 },
      },
    },
    MuiLink: {
      styleOverrides: {
        root: { textDecorationColor: '#C4B5FD', textUnderlineOffset: 3 },
      },
    },
    MuiChip: {
      styleOverrides: {
        root: { fontFamily: '"JetBrains Mono", monospace' },
      },
    },
  },
});
