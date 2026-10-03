import {useState} from 'react'
import {NavLink, Outlet} from 'react-router-dom'

export function AppLayout() {
    const [menuOpen, setMenuOpen] = useState(false)

    function closeMenu() {setMenuOpen(false)}

    return (
        <div className="min-h-screen bg-slate-100">
            <header className="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 md:hidden">
                <button
                    type="button"
                    onClick={() => setMenuOpen(true)}
                    aria-label="Abrir menú"
                    className="rounded-lg p-2 text-slate-700 transition hover:bg-slate-100"
                >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2"
                        className="h-6 w-6"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            d="M4 6h16M4 12h16M4 18h16"
                        />
                    </svg>
                </button>

                <h1 className="text-lg font-bold text-slate-900">
                    Lebane
                </h1>

                <div className="h-10 w-10" />
            </header>

            <div className="flex min-h-[calc(100vh-65px)] md:min-h-screen">

                <aside className="hidden w-64 shrink-0 border-r border-slate-200 bg-white md:block">
                    <SidebarContent />
                </aside>
                {menuOpen && (
                    <button
                        type="button"
                        aria-label="Cerrar menú"
                        onClick={closeMenu}
                        className="fixed inset-0 z-40 bg-slate-900/40 md:hidden"
                    />
                )}

                <aside
                    className={[
                        'fixed inset-y-0 left-0 z-50 w-72 bg-white shadow-xl transition-transform duration-200 md:hidden',
                        menuOpen
                            ? 'translate-x-0'
                            : '-translate-x-full',
                    ].join(' ')}
                >
                    <div className="flex items-center justify-between border-b border-slate-200 px-5 py-4">
                        <h1 className="text-xl font-bold text-slate-900">
                            Lebane
                        </h1>

                        <button
                            type="button"
                            onClick={closeMenu}
                            aria-label="Cerrar menú"
                            className="rounded-lg p-2 text-slate-600 transition hover:bg-slate-100"
                        >
                            <svg
                                xmlns="http://www.w3.org/2000/svg"
                                viewBox="0 0 24 24"
                                fill="none"
                                stroke="currentColor"
                                strokeWidth="2"
                                className="h-5 w-5"
                            >
                                <path
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                    d="M6 18 18 6M6 6l12 12"
                                />
                            </svg>
                        </button>
                    </div>

                    <SidebarContent onNavigate={closeMenu} />
                </aside>

                {/* Content */}
                <main className="min-w-0 flex-1">
                    <div className="mx-auto max-w-7xl p-4 sm:p-6 md:p-8">
                        <Outlet />
                    </div>
                </main>
            </div>
        </div>
    )
}

type SidebarContentProps = {
    onNavigate?: () => void
}

function SidebarContent({onNavigate}: SidebarContentProps) {
    return (
        <>
            <div className="hidden border-b border-slate-200 px-6 py-5 md:block">
                <h1 className="text-xl font-bold text-slate-900">
                    Lebane
                </h1>
            </div>

            <nav className="p-4">
                <NavLink
                    to="/departamentos"
                    onClick={onNavigate}
                    className={({isActive}) =>
                        [
                            'block rounded-lg px-4 py-3 text-sm font-medium transition',
                            isActive
                                ? 'bg-blue-50 text-blue-700'
                                : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900',
                        ].join(' ')
                    }
                >
                    Departamentos
                </NavLink>
            </nav>
        </>
    )
}