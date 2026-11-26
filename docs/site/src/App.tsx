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
import CodeBlock from './components/CodeBlock';
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
  const [active, setActive] = useState(sections[0].id);

  const goTo = (id: string) => {
    setActive(id);
    setDrawerOpen(false);
    const target = document.getElementById(id);
    if (target) {
      const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
      target.scrollIntoView({ behavior: reduced ? 'auto' : 'smooth' });
    }
  };

  const drawer = (
    <div>
      <Toolbar />
      <List aria-label="Daftar bagian dokumentasi">
        {sections.map((section) => (
          <ListItemButton
            key={section.id}
            selected={active === section.id}
            onClick={() => goTo(section.id)}
            aria-current={active === section.id ? 'page' : undefined}
          >
            <ListItemText primary={section.label} />
          </ListItemButton>
        ))}
      </List>
      <Divider sx={{ my: 1 }} />
      <Box sx={{ px: 2 }}>
        <Typography variant="caption" color="text.secondary">
          Versi dokumentasi mengikuti branch main.
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

        <Box component="main" sx={{ flexGrow: 1, minWidth: 0, p: { xs: 2, md: 4 } }}>
          <Toolbar />
          <Container maxWidth="md" disableGutters>
            <Typography variant="h1" gutterBottom>
              Dokumentasi PustakaDigital Desktop
            </Typography>
            <Typography variant="body1" color="text.secondary" component="p" sx={{ mb: 2 }}>
              Alur kerja, peta kode, aturan data, dan panduan pemakaian aplikasi. Istilah yang tampil di antarmuka diberi penanda ungu.
            </Typography>

            <Stack spacing={6} sx={{ mt: 5 }}>
              {sections.map((section) => (
                <Box component="section" key={section.id} id={section.id} sx={{ scrollMarginTop: 88 }}>
                  <Typography variant="h2" gutterBottom>
                    {section.title}
                  </Typography>

                  {section.paragraphs.map((paragraph, index) => (
                    <Typography key={index} variant="body1" component="p" sx={{ mb: 2 }}>
                      {marked(paragraph)}
                    </Typography>
                  ))}

                  {section.steps && (
                    <Stack spacing={2} sx={{ mt: 2 }}>
                      {section.steps.map((step, index) => (
                        <Box
                          key={step.title}
                          sx={{ display: 'flex', gap: 2, p: 2, borderRadius: 2, bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}
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
                            {index + 1}
                          </Box>
                          <Box>
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
                    <Box component="ul" sx={{ mt: 2, pl: 3, color: 'text.secondary' }}>
                      {section.bullets.map((bullet, index) => (
                        <Typography key={index} component="li" variant="body2" sx={{ mb: 1 }}>
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
                </Box>
              ))}
            </Stack>

            <Divider sx={{ my: 5 }} />
            <Typography variant="body2" color="text.secondary">
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
