import { Link, Route, Routes, useLocation } from 'react-router-dom';
import Home from './pages/Home';
import History from './pages/History';

const App = () => {
  const location = useLocation();

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <h1>AI Code Reviewer</h1>
          <p className="subtitle">Automated yet opinionated review feedback</p>
        </div>
        <nav className="nav-links">
          <Link className={location.pathname === '/' ? 'active' : ''} to="/">
            New Review
          </Link>
          <Link className={location.pathname === '/history' ? 'active' : ''} to="/history">
            Past Reviews
          </Link>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/history" element={<History />} />
        </Routes>
      </main>
    </div>
  );
};

export default App;
