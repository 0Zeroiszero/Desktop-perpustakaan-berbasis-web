import { Fragment, useState } from 'react';
import AppBar from '@mui/material/AppBar';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Divider from '@mui/material/Divider';
import Drawer from '@mui/material/Drawer';
import IconButton from '@mui/material/IconButton';
import Link from '@mui/material/Link';
import List from '@mui/material/List';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import Stack from '@mui/material/Stack';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import useMediaQuery from '@mui/material/useMediaQuery';
import { useTheme } from '@mui/material/styles';
import MenuIcon from '@mui/icons-material/Menu';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import CodeBlock from './components/CodeBlock';
import MermaidDiagram from './components/MermaidDiagram';
import UiMark from './components/UiMark';
import { sections } from './data/content';

const DRAWER_WIDTH = 260;

const UI_TERMS = [
  'Kelola Pengguna',
  'Kelola Koleksi',
  'Katalog Buku',
  'Buku tidak tersedia',
  'Baca Sekarang',
  'Baca Ulang',
  'Tambah ke Rak',
  'Hapus dari Rak',
  'Koleksi terkait',
  'Tidak Tertarik',
  'Sedang dibaca',
  'Akan dibaca',
  'Telah dibaca',
  'Berikutnya',
  'Sebelumnya',
  'Tambah Buku',
  'Rak Saya',
  'Dashboard',
  'Lanjutkan',
  'Simpan',
  'Kembali',
  'Keluar',
  'Close',
].sort((a, b) => b.length - a.length);

const escape = (value: string) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
const termPattern = new RegExp(`(${UI_TERMS.map(escape).join('|')})`, 'gi');

function marked(text: string) {
  return text.split(termPattern).map((part, index) =>
    UI_TERMS.some((term) => term.toLowerCase() === part.toLowerCase()) ? (
      <UiMark key={index}>{part}</UiMark>
    ) : (
      <Fragment key={index}>{part}</Fragment>
    ),
  );
}

export default function App() {
  const theme = useTheme();
  const desktop = useMediaQuery(theme.breakpoints.up('md'));
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [index, setIndex] = useState(0);
  const section = sections[index];
  const previous = sections[index - 1];
  const next = sections[index + 1];

  const goTo = (target: number) => {
    setIndex(target);
    setDrawerOpen(false);
    const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    window.scrollTo({ top: 0, behavior: reduced ? 'auto' : 'smooth' });
  };

  const drawer = (
    <div>
      <Toolbar />
      <List aria-label="Daftar bagian dokumentasi">
        {sections.map((item, itemIndex) => (
          <ListItemButton
            key={item.id}
            selected={index === itemIndex}
            onClick={() => goTo(itemIndex)}
            aria-current={index === itemIndex ? 'page' : undefined}
          >
            <ListItemText primary={item.label} />
          </ListItemButton>
        ))}
      </List>
      <Divider sx={{ my: 1 }} />
      <Box sx={{ px: 2 }}>
        <Typography variant="caption" color="text.secondary">
          Bagian {index + 1} dari {sections.length}.
        </Typography>
      </Box>
    </div>
  );

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
      <AppBar position="fixed" sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
        <Toolbar>
          {!desktop && (
            <IconButton
              color="inherit"
              aria-label="Buka daftar isi"
              edge="start"
              onClick={() => setDrawerOpen(true)}
              sx={{ mr: 1 }}
            >
              <MenuIcon />
            </IconButton>
          )}
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 700 }}>
            PustakaDigital Docs
          </Typography>
          <Button
            color="inherit"
            size="small"
            component="a"
            href="https://github.com/0Zeroiszero/Desktop-perpustakaan-berbasis-web"
            target="_blank"
            rel="noopener noreferrer"
          >
            GitHub
          </Button>
        </Toolbar>
      </AppBar>

      <Box sx={{ display: 'flex' }}>
        <Drawer
          variant={desktop ? 'permanent' : 'temporary'}
          open={desktop || drawerOpen}
          onClose={() => setDrawerOpen(false)}
          ModalProps={{ keepMounted: true }}
          sx={{
            width: DRAWER_WIDTH,
            flexShrink: 0,
            '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box', borderRight: '1px solid #E2E8F0' },
          }}
        >
          {drawer}
        </Drawer>

        <Box component="main" sx={{ flexGrow: 1, minWidth: 0, p: { xs: 2, sm: 3, md: 4 } }}>
          <Toolbar />
          <Container maxWidth="md" disableGutters>
            <Typography variant="overline" color="primary" sx={{ fontWeight: 700 }}>
              Bagian {index + 1} / {sections.length}
            </Typography>
            <Typography variant="h1" gutterBottom>
              {section.title}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
              Istilah yang tampil di antarmuka aplikasi diberi penanda ungu.
            </Typography>

            {section.paragraphs.map((paragraph, paragraphIndex) => (
              <Typography key={paragraphIndex} variant="body1" component="p" sx={{ mb: 2 }}>
                {marked(paragraph)}
              </Typography>
            ))}

            {section.diagram && (
              <Box sx={{ my: 3 }}>
                <Typography variant="h3" gutterBottom>
                  Diagram alur
                </Typography>
                <MermaidDiagram chart={section.diagram} />
              </Box>
            )}

            {section.steps && (
              <Stack spacing={2} sx={{ mt: 3 }}>
                {section.steps.map((step, stepIndex) => (
                  <Box
                    key={step.title}
                    sx={{
                      display: 'flex',
                      gap: 2,
                      p: 2,
                      borderRadius: 2,
                      bgcolor: 'background.paper',
                      border: '1px solid',
                      borderColor: 'divider',
                    }}
                  >
                    <Box
                      sx={{
                        width: 28,
                        height: 28,
                        flexShrink: 0,
                        borderRadius: '50%',
                        bgcolor: '#EDE9FE',
                        color: '#5B21B6',
                        display: 'grid',
                        placeItems: 'center',
                        fontWeight: 700,
                        fontSize: 13,
                      }}
                    >
                      {stepIndex + 1}
                    </Box>
                    <Box sx={{ minWidth: 0 }}>
                      <Typography variant="h3" gutterBottom>
                        {marked(step.title)}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {marked(step.text)}
                      </Typography>
                    </Box>
                  </Box>
                ))}
              </Stack>
            )}

            {section.bullets && (
              <Box component="ul" sx={{ mt: 3, pl: 3, color: 'text.secondary' }}>
                {section.bullets.map((bullet, bulletIndex) => (
                  <Typography key={bulletIndex} component="li" variant="body2" sx={{ mb: 1, wordBreak: 'break-word' }}>
                    {marked(bullet)}
                  </Typography>
                ))}
              </Box>
            )}

            {section.table && (
              <Box sx={{ mt: 3, overflowX: 'auto', borderRadius: 2, border: '1px solid', borderColor: 'divider' }}>
                <Table size="small">
                  <TableHead sx={{ bgcolor: '#F1F5F9' }}>
                    <TableRow>
                      {section.table.head.map((cell) => (
                        <TableCell key={cell} sx={{ fontWeight: 700 }}>
                          {cell}
                        </TableCell>
                      ))}
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {section.table.rows.map((row, rowIndex) => (
                      <TableRow key={rowIndex}>
                        {row.map((cell, cellIndex) => (
                          <TableCell key={cellIndex}>{marked(cell)}</TableCell>
                        ))}
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </Box>
            )}

            {section.entries && (
              <Stack spacing={2} sx={{ mt: 3 }}>
                {section.entries.map((entry) => (
                  <CodeBlock key={entry.symbol} entry={entry} />
                ))}
              </Stack>
            )}

            <Divider sx={{ my: 5 }} />
            <Stack
              direction={{ xs: 'column', sm: 'row' }}
              spacing={2}
              sx={{ justifyContent: 'space-between', alignItems: { sm: 'center' } }}
            >
              <Button
                variant="outlined"
                startIcon={<ArrowBackIcon />}
                disabled={!previous}
                onClick={() => previous && goTo(index - 1)}
                fullWidth={!desktop}
              >
                Sebelumnya
              </Button>
              <Button
                variant="contained"
                endIcon={<ArrowForwardIcon />}
                disabled={!next}
                onClick={() => next && goTo(index + 1)}
                fullWidth={!desktop}
              >
                {next ? `Berikutnya: ${next.label}` : 'Selesai'}
              </Button>
            </Stack>

            <Typography variant="body2" color="text.secondary" sx={{ mt: 5 }}>
              Sumber kode:{' '}
              <Link href="https://github.com/0Zeroiszero/Desktop-perpustakaan-berbasis-web" target="_blank" rel="noopener noreferrer">
                Desktop-perpustakaan-berbasis-web
              </Link>
              . Dokumentasi ini dibangun dengan Material UI.
            </Typography>
          </Container>
        </Box>
      </Box>
    </Box>
  );
}
